package com.moore.tools.easynetty.process;


import com.moore.tools.easynetty.service.NettyHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author ：imoore
 * @date ：created in 2023/11/9 22:29
 * @description：客户端
 * @version: v1
 */
@Slf4j
public class Client {

    /**
     * 业务处理线程数量一般是1：4或者1：8
     */
    private final static Integer WORKER_GROUP_THREADS = 4;
    private static Bootstrap clientBootstrap;
    //是否已配置
    private static boolean isConfigured = false;

    private static EventLoopGroup workerGroup;
    public static ChannelFuture channelFuture;
    public static SendHelper sendHelper;

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
     * 根据需求自定义构建
     *
     * @param config
     */
    public static void customizable(Consumer<Bootstrap> config) {
        config.accept(instance());
        if (!isConfigured) {
            isConfigured = true;
        }
    }

    /**
     * 对处理器进行自定义配置
     *
     * @param impl 自定义处理器实例
     */
    public static void customizableHandler(Supplier<ChannelHandler> impl) {
        customizable(bootstrap -> {
            workerGroup = new NioEventLoopGroup(WORKER_GROUP_THREADS);
            //设置链接组合业务处理组
            bootstrap.group(workerGroup)
                    //使用NIO非阻塞通信
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
            log.error("client non instance");
            return true;
        }
        if (!isConfigured) {
            log.error("client not configured");
            return true;
        }
        return false;
    }

    /**
     * 连接服务器
     *
     * @param port 端口号
     */
    public synchronized static void connect(String ipAddress, int port) {
        if (isInvalid()) {
            return;
        }
        try {

            channelFuture = clientBootstrap.connect(ipAddress, port).sync();
            sendHelper = new SendHelper(channelFuture.channel());
            // 等待连接关闭
//            channelFuture.channel().closeFuture().sync();
            log.info("client started on {}:{}.", ipAddress, port);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
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

    public synchronized static void stop() {
        if (isInvalid()) {
            return;
        }
        if (channelFuture.channel().isActive()) {
            log.info("client stopped!");
            channelFuture.channel().close();
        }
        workerGroup.shutdownGracefully();
    }


    /**
     * 发送消息无回调
     *
     * @param msg 消息
     */
    public static void sendWithoutCallback(String msg) {
        sendWithCallback(msg, () -> {
        });
    }

    /**
     * 发送消息 有回调
     *
     * @param msg      消息
     * @param callBack 发送完执行
     */
    public static void sendWithCallback(String msg, Runnable callBack) {
        try {
            sendHelper.send(msg, callBack);
        } catch (RuntimeException e) {
            log.error("Not connected to the server. Call connect() first.");
        }

    }

    @AllArgsConstructor
    private static class SendHelper {
        private final Channel senderChanel;

        /**
         * 发送消息
         *
         * @param msg 消息
         * @param run callback
         */
        public void send(String msg, Runnable run) {
            if (Objects.isNull(msg)) {
                return;
            }
            if (isInvalid()) {
                return;
            }
            CompletableFuture.runAsync(() -> {
                NettyHelper.sendWithSequence(senderChanel, msg);
                log.info("Message sent:" + msg);
            }).thenRun(run).exceptionally(ex -> {
                log.error("send error:{}", ex.getMessage(), ex);
                return null;
            });
        }

    }

}
