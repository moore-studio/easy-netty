package com.moore.tools.easynetty.service.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;

/**
 * 服务端抽象方法
 *
 * @author ：imoore
 */

public abstract class NettyAbstractServer extends BaseAbstractNetty<ServerBootstrap> {


    public NettyAbstractServer(ServerBootstrap serverBootstrap, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        super(serverBootstrap, bossGroup, workerGroup);
    }

    @Override
    public void createInstance() {
        if (!isConfigured) {
            configured(bootstrap);
            bootstrap.childHandler(channelInitializer());
            isConfigured = true;
        }
    }
}
