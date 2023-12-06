package com.moore.tools.easynetty.process;


import com.moore.tools.easynetty.constants.Constant;
import com.moore.tools.easynetty.service.sendreceive.IExchangeService;
import com.moore.tools.easynetty.uitils.CommonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author ：imoore
 * @date ：created in 2023/11/9 22:29
 * @description：客户端
 * @version: v1
 */
@Slf4j
public class NettyClient<E extends IExchangeService> {

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
    public static Class<? extends IExchangeService> exchangeClass;
    private static NettyClient nettyInstance;
    /**
     * 信息收发实例
     */
    private E exchange;


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
                    //nettyInstance = new NettyClient();

                }
            }
        }
        return clientBootstrap;
    }

    /**
     * 绑定消息收发
     *
     * @param clazz 消息首发类
     * @param <E>   实体
     */
    public static <E extends IExchangeService> void bindExchange(Class<E> clazz) {
        exchangeClass = clazz;
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
            log.info("netty server init done");
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
                log.info("client started on {}:{}.", ipAddress, port);
                break;
            } catch (Exception e) {
                retryCount++;
                log.error("connected failed reason is " + e.getMessage(), e);
                log.warn("retry {}", retryCount);

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

    public static <E extends IExchangeService> E exchange() {
        if (Objects.isNull(exchangeClass)) {
            log.error("IExchangeServer not instantiated. Please bind the message exchange class using bindExchange(impl)");
            return null;
        }
        return (E) nettyInstance.exchange;
    }

}
