package com.moore.tools.easynetty.zexample;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.service.channelhandler.BaseAbstractReceiverHandler;
import com.moore.tools.easynetty.service.exchange.entity.NioMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author ：imoore
 */
@Slf4j
public class ExampleClientHandler extends BaseAbstractReceiverHandler {
    public ExampleClientHandler(String identityId) {
        super(identityId);
    }

    @Override
    public void receiveMessage(ChannelHandlerContext channel, NioMessage message) {
        log.info("receive msg:{}", JSON.toJSONString(message));
    }

    @Override
    public void connected(ChannelHandlerContext channel) {
        log.info("have a Connection");
        sender.send(channel.channel(),new NioMessage(identityId,"","INIT_CLIENT_IDENTIFY_ID"));
    }

    @Override
    public void disconnected(ChannelHandlerContext channel) {
        log.info("lost a Connection");
    }

    @Override
    public void receiveCompleted(ChannelHandlerContext channel) {

    }

    @Override
    public void exception(ChannelHandlerContext channel, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void heartBeatWriter(ChannelHandlerContext ctx, IdleStateEvent event) {
        sender.send(ctx.channel(), new NioMessage(identityId, UUID.randomUUID().toString(), "HEART_BEATS"));
    }
}
