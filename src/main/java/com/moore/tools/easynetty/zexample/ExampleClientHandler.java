package com.moore.tools.easynetty.zexample;

import com.moore.tools.easynetty.service.channelhandler.AbstractReceiveHandler;
import com.moore.tools.easynetty.service.channelhandler.BaseAbstractReceiverHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 */
@Slf4j
public class ExampleClientHandler extends BaseAbstractReceiverHandler {
    @Override
    public void receiveMessage(Channel channel, String sequence, Object data) {
        log.info("receive msg:{}:{}",sequence,data);
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
