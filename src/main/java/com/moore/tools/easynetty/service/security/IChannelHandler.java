package com.moore.tools.easynetty.service.security;

import com.moore.tools.easynetty.entities.NettyEntity;
import io.netty.channel.Channel;

public interface IChannelHandler {
    /**
     * 客户端连接激活
     */
    void active(Channel channel);

    /**
     * 断开
     */
    void inactive(Channel channel);

    /**
     * 接收消息
     */
    <R extends NettyEntity> void received(Channel channel, R entity);

    /**
     * 异常
     */
    void exceptionCaught(Channel channel,Throwable throwable);
}
