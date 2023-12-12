package com.moore.tools.easynetty.service;

import com.moore.tools.easynetty.common.enums.ErrorMessageEnum;
import com.moore.tools.easynetty.common.exceptions.EasyNettyException;
import com.moore.tools.easynetty.service.exchange.send.ISender;
import com.moore.tools.easynetty.service.netty.NettyAbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * netty客户端类
 * @author ：imoore
 * @date ：created in 2023/12/10 15:23
 */
@Slf4j
public class NettyClient extends NettyAbstractClient {
    /**
     * 业务处理线程数量一般是1：4或者1：8
     */
    private final static Integer WORKER_GROUP_THREADS = 4;
    private ChannelHandlerAdapter channelHandler;
    private ISender sender;

    public NettyClient() {
        super(new Bootstrap(), new NioEventLoopGroup(WORKER_GROUP_THREADS));
    }

    /**
     * 实例配置
     * @param bootstrap netty实例
     */
    @Override
    public void configured(Bootstrap bootstrap) {
        bootstrap.group(workerGroup)
                //使用NIO非阻塞通信
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class);
    }

    @Override
    public ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                //防止粘包，消息结尾追加换行符 一次解码最多处理8192个字节
                pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                Optional.ofNullable(channelHandler).ifPresent(pipeline::addLast);
            }
        };
    }

    /**
     * 追加信道处理器
     * @param channelHandler 处理器
     * @return this
     */
    public NettyClient addChannelHandler(Supplier<? extends ChannelHandlerAdapter> channelHandler) {
        this.channelHandler = Optional.ofNullable(channelHandler.get()).orElseThrow(() -> new EasyNettyException(ErrorMessageEnum.NO_CHANNEL_HANDLER.formatter("Client")));
        return this;
    }

    /**
     * 绑定消息发送
     * @param senderImpl 消息发送实体
     * @return this
     */
    public NettyClient bind(ISender senderImpl) {
        sender = senderImpl;
        return this;
    }

    /**
     * 连接服务器
     * @param ipAddress ip地址
     * @param port 端口
     */
    public void connect(String ipAddress, int port) {
        //拉起服务创建连接
        createInstance();
        connectImpl(ipAddress, port);
    }

    /**
     * 连接服务器
     * TODO:后续要把重试，断连重试及间隔时间写到配置项
     * 连接的重试次数是5次
     * 间隔时间是3秒
     * 断连重试间隔时间是10秒
     *
     * @param ipAddress ip地址
     * @param port      端口号
     */
    private synchronized void connectImpl(String ipAddress, int port) {
        if (isInvalid()) {
            return;
        }

        //服务器无法连接&重试
        //TODO:整理成配置文件格式
        int retryCount = 0;
        int connectMaxRetries = 5;
        int connectDelayTime = 3;
        boolean isConnected = false;

        while (retryCount < connectMaxRetries && !isConnected) {
            try {
                channelFuture = bootstrap.connect(ipAddress, port).sync();
                isConnected = true;
                log.debug("Client started on {}:{}.", ipAddress, port);
                break;
            } catch (Exception e) {
                retryCount++;
                log.error("Connected failed reason is " + e.getMessage(), e);
                log.warn("Retry attempt {}", retryCount);

                try {
                    Thread.sleep(connectDelayTime * 1000);
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            } finally {
                if (channelFuture != null && !channelFuture.isSuccess()) {
                    channelFuture.channel().close();
                }
            }
        }
        if (isConnected) {
            //nettyInstance.exchange = CommonUtils.tryNewInstance(exchangeClass, new Class<?>[]{Channel.class}, channelFuture.channel());
            //添加监视，断开重连 需要配合心跳检测
            channelFuture.addListener((future -> {
                if (!future.isSuccess()) {
                    log.error("Connection attempt failed: {}", future.cause().getMessage());
                    // 连接失败时进行重连，可以选择延迟一段时间后再次尝试
                    scheduleReconnect(ipAddress, port, 10);
                } else {
                    log.debug("Client connected to {}:{}", ipAddress, port);
                }
            }));
        }

    }

    /**
     * 消息发送
     * @param sequence 序列
     * @param message 消息
     */
    public void send(String sequence, String message) {
        sendImpl(sequence, message);
    }

    /**
     * 发送消息
     * @param message 消息
     */
    public void send(String message) {
        send("", message);
    }

    /**
     * 发送实现
     * @param sequence 序列号
     * @param message 消息
     */
    private void sendImpl(String sequence, String message) {
        if (Objects.isNull(sender)) {
            log.error("ISender not be implemented,please bind first ");
            return;
        }
        if (isInactive()) {
            log.error("Client not started,channel is inactive.");
            return;
        }
        sender.send(channelFuture.channel(), sequence, message);
    }

    /**
     * 断开重连
     *
     * @param ipAddress   ip地址
     * @param port        端口号
     * @param delaySecond 重连时间(秒)
     */
    private void scheduleReconnect(String ipAddress, int port, int delaySecond) {
        // 在连接失败后，延迟一段时间后进行重连
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.schedule(() -> {
            connectImpl(ipAddress, port);
            executorService.shutdown(); // 重连后关闭定时任务
        }, delaySecond, TimeUnit.SECONDS);
    }
}