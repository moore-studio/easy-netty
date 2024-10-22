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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * netty客户端类
 *
 * @author ：imoore
 * @date ：created in 2023/12/10 15:23
 */
@Slf4j
public class NettyClient extends NettyAbstractClient {
    /**
     * 业务处理线程数量一般是1：4或者1：8
     */
    private final static Integer WORKER_GROUP_THREADS = 4;
    /**
     * 消息适配器
     */
    private ChannelHandlerAdapter channelHandler;
    /**
     * 消息发送接口
     */
    private ISender sender;
    /**
     * ipaddress
     */
    private String ipAddress = null;
    /**
     * port
     */
    private int port = 0;
    /**
     * 内部重连次数
     */
    private int internalRetryCount = 0;
    /**
     * 定时job监控器 重连监控
     */
    private ScheduledExecutorService executorService;
    /**
     * 最大重试次数
     */
    static int CONNECTED_MAX_RETRIES = 5;
    /**
     * 间隔？s启动重试服务
     */
    static int CONNECTED_DELAY_TIME = 3;

    public NettyClient() {
        super(new Bootstrap(), new NioEventLoopGroup(WORKER_GROUP_THREADS));

    }

    /**
     * 实例配置
     *
     * @param bootstrap netty实例
     */
    @Override
    public void configured(Bootstrap bootstrap) {
        bootstrap.group(workerGroup)
                //使用NIO非阻塞通信
                .option(ChannelOption.SO_KEEPALIVE, true).channel(NioSocketChannel.class);
    }

    @Override
    public ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                //防止粘包，消息结尾追加换行符 一次解码最多处理8192个字节
//                pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                Optional.ofNullable(channelHandler).ifPresent(pipeline::addLast);
            }
        };
    }

    /**
     * 追加信道处理器
     *
     * @param channelHandler 处理器
     * @return this
     */
    public NettyClient addChannelHandler(Supplier<? extends ChannelHandlerAdapter> channelHandler) {
        this.channelHandler = Optional.ofNullable(channelHandler.get()).orElseThrow(() -> new EasyNettyException(ErrorMessageEnum.NO_CHANNEL_HANDLER.formatter("Client")));
        return this;
    }

    /**
     * 绑定消息发送
     *
     * @param senderImpl 消息发送实体
     * @return this
     */
    public NettyClient bind(ISender senderImpl) {
        sender = senderImpl;
        return this;
    }

    /**
     * 连接服务器
     *
     * @param ipAddress ip地址
     * @param port      端口
     */
    public void connect(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        //拉起服务创建连接
        createInstance();
        retryChecking();
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
        try {
            channelFuture = bootstrap.connect(ipAddress, port).sync();
            sender.addChannel(channelFuture.channel());
            log.info("Client started on {}:{}.", ipAddress, port);
        } catch (Exception e) {
            log.error("Connected failed reason is " + e.getMessage(), e);
        } finally {
            if (channelFuture != null && !channelFuture.isSuccess()) {
                channelFuture.channel().close();
            }
        }
    }

    /**
     * 消息发送
     *
     * @param sequence 序列
     * @param message  消息
     */
    public void send(String sequence, String message) {
        sendImpl(sequence, message);
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    public void send(String message) {
        send("", message);
    }

    /**
     * 发送实现
     *
     * @param sequence 序列号
     * @param message  消息
     */
    private void sendImpl(String sequence, String message) {
        if (Objects.isNull(sender)) {
            log.error("ISender not be implemented,please bind first ");
            return;
        }
        if (isInactive()) {
            log.warn("Client not started,channel is inactive.");
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

        executorService.scheduleAtFixedRate(() -> {
            if (StringUtils.isEmpty(ipAddress) || port == 0) {
                log.debug("Client non instance.");
                return;
            }
            if (!isInactive()) {
                //HEART_BEET_PACKAGE
                heartBeatPackage();
                if (internalRetryCount > 0) {
                    internalRetryCount = 0;
                }
                log.debug("Server status : OK.");
                return;
            }
            internalRetryCount++;
            log.warn("Unable to connect to the server, try to connect,retries:{} / {}", internalRetryCount, CONNECTED_MAX_RETRIES);
            connectImpl(ipAddress, port);
            if (internalRetryCount >= CONNECTED_MAX_RETRIES) {
                log.info("Retry service shutdown.");
                // 重连后关闭定时任务
                executorService.shutdown();
            }

            //
        }, CONNECTED_DELAY_TIME, delaySecond, TimeUnit.SECONDS);
    }

    public void retryChecking() {
        // 在连接失败后，延迟一段时间后进行重连
        executorService = Executors.newScheduledThreadPool(1);
        scheduleReconnect(ipAddress, port, 10);
    }

    public void heartBeatPackage() {
        send("HEART_BATE_PACKAGE_" + System.currentTimeMillis());
    }

    @Override
    public void stop() {
        super.stop();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (sender.getScheduleExecutorService().isShutdown()) {
            return;
        }
        sender.getScheduleExecutorService().shutdown();
    }
}
