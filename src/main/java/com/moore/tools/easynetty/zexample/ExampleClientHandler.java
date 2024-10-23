package com.moore.tools.easynetty.zexample;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.service.channelhandler.AbstractReceiveHandler;
import com.moore.tools.easynetty.service.channelhandler.BaseAbstractReceiverHandler;
import com.moore.tools.easynetty.service.exchange.NioMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 */
@Slf4j
public class ExampleClientHandler extends BaseAbstractReceiverHandler {
    @Override
    public void receiveMessage(Channel channel, NioMessage message) {
        log.info("receive msg:{}", JSON.toJSONString(message));
    }

    @Override
    public void connected(Channel channel) {
        log.info("have a Connection");
    }

    @Override
    public void disconnected(Channel channel) {
        log.info("lost a Connection");
    }

    @Override
    public void receiveCompleted(Channel channel) {

    }

    @Override
    public void exception(Channel channel, Throwable cause) {
        cause.printStackTrace();
    }
}
