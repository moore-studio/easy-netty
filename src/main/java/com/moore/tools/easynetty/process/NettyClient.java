package com.moore.tools.easynetty.process;


import com.moore.tools.easynetty.constants.Constant;
import com.moore.tools.easynetty.service.exchange.send.ISender;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 客户端实例
 * @author ：imoore
 * @date ：created in 2023/11/9 22:29
 * @version: v1
 */
@Slf4j
public class NettyClient {

    /**
     * 业务处理线程数量一般是1：4或者1：8
     */
    private final static Integer WORKER_GROUP_THREADS = 4;
    /**
     * 客户端服务
     */
    private static volatile Bootstrap clientBootstrap;
    /**
     * 客户端是否已配置完成
     */
    private static boolean isConfigured = false;
    private static EventLoopGroup workerGroup;
    public static ChannelFuture channelFuture;
    /**
     * 信息收发类
     */
    private static ISender sender;

    /**
     * 客户端实例创建
     *
     * @return Bootstrap
     */
    private static Bootstrap instance() {
        if (Objects.isNull(clientBootstrap)) {
            synchronized (ServerBootstrap.class) {
                if (Objects.isNull(clientBootstrap)) {
                    clientBootstrap = new Bootstrap();
                }
            }
        }
        return clientBootstrap;
    }


    /**
     * 自定义bootstrap配置
     *
     * @param workerThreads 业务线程数量
     * @param config        bootstrap
     */
    public synchronized static void customizable(int workerThreads, Consumer<Bootstrap> config) {
        if (workerThreads == Constant.INVALID_THREADS) {
            workerThreads = WORKER_GROUP_THREADS;
        }
        workerGroup = new NioEventLoopGroup(workerThreads);
        config.accept(instance());
        if (!isConfigured) {
            isConfigured = true;
        }
    }


    /**
     * 对处理器进行自定义配置
     *
     * @param impl 处理器实例
     */
    public static void customizableHandler(Supplier<ChannelHandler> impl) {
        customizableHandler(Constant.INVALID_THREADS, impl);
    }

    /**
     * 对处理器进行自定义配置
     *
     * @param workerThreads 业务处理线程数
     * @param impl          处理器实例
     */
    public static void customizableHandler(int workerThreads, Supplier<ChannelHandler> impl) {
        customizable(workerThreads, bootstrap -> {

            //设置链接组合业务处理组
            bootstrap.group(workerGroup)
                    //使用NIO非阻塞通信
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class).handler(impl.get());
            log.info("Netty server init done");
        });
    }

    /**
     * 默认构建
     *
     * @param impl 设置netty的处理器
     */
    public static void build(Supplier<ChannelInboundHandlerAdapter> impl) {
        customizableHandler(() -> new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                pipeline.addLast(impl.get());
            }
        });
    }

    /**
     * 是否被实例化
     *
     * @return bool
     */
    public static boolean hasInstance() {
        return Objects.nonNull(clientBootstrap);
    }


    /**
     * 是否有效
     *
     * @return bool
     */
    private synchronized static boolean isInvalid() {
        if (!hasInstance()) {
            log.error("Client instance is null");
            return true;
        }
        if (!isConfigured) {
            log.error("Client is not configured");
            return true;
        }
        return false;
    }


    /**
     * 绑定消息收发
     *
     * @param sendImpl 消息首发类
     * @param <E>      实体
     */
    public static <E extends ISender> void bind(E sendImpl) {
        sender = sendImpl;
    }

    private static boolean isActive() {
        return Objects.nonNull(channelFuture) && Objects.nonNull(channelFuture.channel()) && channelFuture.channel().isActive();
    }

    /**
     * 消息发送
     * @param message
     */
    public static void send(String message) {
        if(Objects.isNull(sender)){
            log.error("ISender not be implemented,please bind first ");
            return;
        }
        if (!isActive()) {
            log.error("Client not started,channel is inactive.");
            return;
        }
        sender.send(channelFuture.channel(), null, message);
    }

    /**
     * 消息发送
     * @param message
     */
    public static void send(String sequence,String message) {
        if(Objects.isNull(sender)){
            log.error("ISender not be implemented,please bind first ");
            return;
        }
        if (!isActive()) {
            log.error("Client not started,channel is inactive.");
            return;
        }
        sender.send(channelFuture.channel(), sequence, message);
    }

    /**
     * 连接服务器
     * 必须实现bindExchange来指定客户端收发消息的方法
     * bindExchange extend IExchangeService
     * 客户端中，IExchangeService的实现类中必须保证存在单参数的构造方法（io.netty.channel.Channel）
     * 在与服务器建立连接后，会根据bindExchange指定的类创建一个实例 （new Instance(io.netty.channel.Channel)）
     * 通过 NettyClient.exchange()来发送指令
     *
     * @param port 端口号
     */
    public synchronized static void connect(String ipAddress, int port) {
        if (isInvalid()) {
            return;
        }
        //服务器无法连接&重试
        /**
         * TODO:整理成配置文件格式
         */
        int retryCount = 0;
        int connectMaxRetries = 5;
        int connectDelayTime = 3;
        boolean isConnected = false;

        while (retryCount < connectMaxRetries && !isConnected) {
            try {
                channelFuture = clientBootstrap.connect(ipAddress, port).sync();
                isConnected = true;
                log.info("Client started on {}:{}.", ipAddress, port);
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
                    log.info("Client connected to {}:{}", ipAddress, port);
                }
            }));
        }
    }

    /**
     * 断开重连
     *
     * @param ipAddress   ip地址
     * @param port        端口号
     * @param delaySecond 重连时间(秒)
     */
    private static void scheduleReconnect(String ipAddress, int port, Integer delaySecond) {
        // 在连接失败后，延迟一段时间后进行重连
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.schedule(() -> {
            connect(ipAddress, port);
            executorService.shutdown(); // 重连后关闭定时任务
        }, delaySecond, TimeUnit.SECONDS);
    }

    /**
     * 连接
     *
     * @param chanelHandle chanelhandle
     * @param ipAddress    地址
     * @param port         端口号
     */
    public synchronized static void connect(Supplier<ChannelInboundHandlerAdapter> chanelHandle, String ipAddress, int port) {
        if (isInvalid()) {
            build(chanelHandle);
        }
        connect(ipAddress, port);
    }

    /**
     * 服务停止
     */
    public synchronized static void stop() {
        if (isInvalid()) {
            return;
        }
        try {
            if (channelFuture != null && channelFuture.channel().isActive()) {
                log.info("Client stopped!");
                channelFuture.channel().close().sync();
            }
        } catch (InterruptedException e) {
            log.error("Error while stopping the client: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }


}
