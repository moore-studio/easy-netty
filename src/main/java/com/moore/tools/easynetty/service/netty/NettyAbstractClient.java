package com.moore.tools.easynetty.service.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端抽象方法
 *
 * @author ：imoore
 */

@Slf4j
public abstract class NettyAbstractClient extends BaseAbstractNetty<Bootstrap> {
    public NettyAbstractClient(Bootstrap clientBootstrap, EventLoopGroup workerGroup) {
        super(clientBootstrap, null, workerGroup);
    }

    @Override
    public void createInstance() {
        if (!isConfigured) {
            configured(bootstrap);
            bootstrap.handler(channelInitializer());
            isConfigured = true;
        }
    }
}
