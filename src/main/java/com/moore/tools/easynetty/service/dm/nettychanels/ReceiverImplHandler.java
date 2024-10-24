package com.moore.tools.easynetty.service.dm.nettychanels;

import com.moore.tools.easynetty.service.channelhandler.BaseAbstractReceiverHandler;
import com.moore.tools.easynetty.service.exchange.NioMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 * @date ：created in 2023/12/12 19:23
 * @description：消息接收
 * @version: v
 */
@Slf4j
@Deprecated
public class ReceiverImplHandler extends BaseAbstractReceiverHandler {
    public ReceiverImplHandler(String identityId) {
        super(identityId);
    }

    @Override
    public void receiveMessage(ChannelHandlerContext channel, NioMessage message) {

    }

    @Override
    public void connected(ChannelHandlerContext channel) {

    }

    @Override
    public void disconnected(ChannelHandlerContext channel) {

    }

    @Override
    public void receiveCompleted(ChannelHandlerContext channel) {

    }

    @Override
    public void exception(ChannelHandlerContext channel, Throwable cause) {

    }
}
