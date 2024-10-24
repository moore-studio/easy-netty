package com.moore.tools.easynetty.service;

import com.moore.tools.easynetty.common.constants.LogMessageConstant;
import com.moore.tools.easynetty.common.enums.ErrorMessageEnum;
import com.moore.tools.easynetty.common.exceptions.EasyNettyException;
import com.moore.tools.easynetty.service.netty.NettyAbstractServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * netty服务端类
 *
 * @author ：imoore
 */
@Slf4j
public class NettyServer extends NettyAbstractServer {
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

    public NettyServer() {
        super(new ServerBootstrap(), new NioEventLoopGroup(BOSS_GROUP_THREADS), new NioEventLoopGroup(WORKER_GROUP_THREADS));
    }

    /**
     * 设置标识Id
     *
     * @param identifyId 标识Id
     * @return
     */
    public NettyServer setIdentifyId(String identifyId) {
        this.identifyId = identifyId;
        return this;
    }

    public NettyServer enableHeartBeatChecking() {
        isEnableHeartBeatChecking = true;
        return this;
    }

    @Override
    public void configured(ServerBootstrap serverBootstrap) {
        //设置链接组合业务处理组
        serverBootstrap.group(bossGroup, workerGroup)
                //使用NIO非阻塞通信
                .channel(NioServerSocketChannel.class)
                //设置默认最大连接数
                .option(ChannelOption.SO_BACKLOG, DEFAULT_MAX_CONNECTIONS);
        //.option(ChannelOption.SO_KEEPALIVE, true);
    }

    /**
     * 配置处理器
     *
     * @return 处理器
     */
    @Override
    public ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                if (isEnableHeartBeatChecking) {
                    socketChannel.pipeline().addLast(new IdleStateHandler(10, 5, 0, TimeUnit.SECONDS));
                }
//                socketChannel.pipeline().addLast(new HeartBeatsHandler());
                for (ChannelHandler handler : channelHandlers) {
                    Optional.ofNullable(handler).ifPresent(socketChannel.pipeline()::addLast);
                }

            }
        };
    }

    /**
     * 追加信道处理器
     *
     * @param channelHandler 处理器
     * @return this
     */
    public NettyServer addChannelHandler(Supplier<? extends ChannelHandlerAdapter> channelHandler) {
        this.channelHandlers.add(Optional.ofNullable(channelHandler.get()).orElseThrow(() -> new EasyNettyException(ErrorMessageEnum.NO_CHANNEL_HANDLER.formatter("Client"))));
        return this;
    }

    /**
     * 服务启动
     *
     * @param port 端口号
     */
    public synchronized void start(int port) {
        createInstance();
        if (isInvalid()) {
            return;
        }
        try {
            channelFuture = bootstrap.bind(port).sync();
            log.info(LogMessageConstant.I_NETTY_START, "Server", "localhost", port);
        } catch (InterruptedException e) {
            log.error(LogMessageConstant.E_THROW_ERROR, "Server Started", e.getMessage(), e);
        }
    }
}
