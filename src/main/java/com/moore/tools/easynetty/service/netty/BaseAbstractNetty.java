package com.moore.tools.easynetty.service.netty;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

/**
 * Netty基类
 *
 * @author ：imoore
 */
@Slf4j
public abstract class BaseAbstractNetty<R extends AbstractBootstrap> {
    protected final EventLoopGroup bossGroup;
    protected final EventLoopGroup workerGroup;
    protected final R bootstrap;
    protected boolean isConfigured = false;
    protected ChannelFuture channelFuture;
    /**
     * 处理器
     */
    protected ChannelHandler channelHandler;

    public BaseAbstractNetty(R bootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.bootstrap = bootstrap;
    }

    /**
     * 配置bootstrap
     *
     * @param abstractBootstrap netty实例
     */
    public abstract void configured(R abstractBootstrap);
    /**
     * 配置处理器初始化
     *
     * @return 处理器
     */
    public abstract ChannelHandler channelInitializer();


    /**
     * 构建实例,拉起服务
     */
    public void createInstance() {
        if (!isConfigured) {
            configured(bootstrap);
            isConfigured = true;
        }
    }

    /**
     * 时候配置成功
     *
     * @return true/false
     */
    public boolean isInvalid() {
        if (!isConfigured) {
            log.error("Client is not configured.");
            return true;
        }
        return false;
    }

    /**
     * 当前通道是否激活
     *
     * @return ture/false
     */
    public boolean isInactive() {
        return Objects.isNull(channelFuture) || Objects.isNull(channelFuture.channel()) || !channelFuture.channel().isActive();
    }

    /**
     * 服务停止
     */
    public void stop() {
        if (isInvalid()) {
            log.error("Stop failure: Bootstrap was not configured.");
            return;
        }
        if (isInactive()) {
            log.error("Stop failure: ChannelFuture is inactive.");
            return;
        }
        try {
            channelFuture.channel().close().sync();
            log.info("Client stopped.");
        } catch (InterruptedException e) {
            log.error("Error while stopping the client: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            Optional.ofNullable(bossGroup).ifPresent(EventExecutorGroup::shutdownGracefully);
            Optional.ofNullable(workerGroup).ifPresent(EventExecutorGroup::shutdownGracefully);
        }
    }
}
