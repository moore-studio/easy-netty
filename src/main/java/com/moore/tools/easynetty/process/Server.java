package com.moore.tools.easynetty.process;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author ：imoore
 * @date ：created in 2023/11/9 20:05
 * @description：nettyServer
 * @version: v
 */
@Slf4j
public class Server {
    /**
     * 连接处理线程数量
     */
    private final static Integer BOSS_GROUP_THREADS = 1;
    /**
     * 业务处理线程数量一般是1：4或者1：8
     */
    private final static Integer WORKER_GROUP_THREADS = BOSS_GROUP_THREADS * 8;
    /**
     * 默认最大连接数128个 超过128则自动拒绝
     * 根据业务设置最大连接数
     */
    private final static Integer DEFAULT_MAX_CONNECTIONS = 128;
    private static ServerBootstrap serverBootstrap;
    /**
     * 是否已配置
     */
    private static boolean isConfigured = false;
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    private static ChannelFuture channelFuture;


    /**
     * 服务端实例创建
     *
     * @return ServerBootstrap
     */
    private static ServerBootstrap instance() {
        if (Objects.isNull(serverBootstrap)) {
            synchronized (ServerBootstrap.class) {
                if (Objects.isNull(serverBootstrap)) {
                    serverBootstrap = new ServerBootstrap();
                }
            }
        }
        return serverBootstrap;
    }

    /**
     * 根据需求自定义构建
     *
     * @param config
     */
    public static void customizable(Consumer<ServerBootstrap> config) {
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
            bossGroup = new NioEventLoopGroup(BOSS_GROUP_THREADS);
            workerGroup = new NioEventLoopGroup(WORKER_GROUP_THREADS);
            //设置链接组合业务处理组
            bootstrap.group(bossGroup, workerGroup)
                    //使用NIO非阻塞通信
                    .channel(NioServerSocketChannel.class)
                    //设置默认最大连接数
                    .option(ChannelOption.SO_BACKLOG, DEFAULT_MAX_CONNECTIONS)
                    .childHandler(impl.get());
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
                socketChannel.pipeline().addLast(impl.get());
            }
        });
    }

    /**
     * 是否被实例化
     *
     * @return bool
     */
    public static boolean nonInstance() {
        return !Objects.nonNull(serverBootstrap);
    }

    /**
     * 服务启动
     *
     * @param port 端口号
     */
    public synchronized static void start(int port) {
        if (nonInstance()) {
            log.error("server non instance");
            return;
        }
        if (!isConfigured) {
            log.error("server not configured");
            return;
        }
        try {
            channelFuture = serverBootstrap.bind(port).sync();
            log.info("Server started on port {}.", port);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 启动
     * @param channelHandle 配置channelHandle
     * @param port 端口号
     */
    public synchronized static void start(Supplier<ChannelInboundHandlerAdapter> channelHandle, int port) {
        if (nonInstance()) {
            build(channelHandle);
        }
        start(port);
    }


    /**
     * 关闭服务
     */
    public synchronized static void stop() {
        if (nonInstance()) {
            log.error("server non instance");
            return;
        }
        if (!isConfigured) {
            log.error("server not configured");
            return;
        }
        if (channelFuture.channel().isActive()) {
            log.info("server stopped.");
            channelFuture.channel().close();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

    }

}

