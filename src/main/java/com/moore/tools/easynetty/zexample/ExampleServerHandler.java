package com.moore.tools.easynetty.zexample;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.common.constants.Constant;
import com.moore.tools.easynetty.service.channelhandler.BaseAbstractReceiverHandler;
import com.moore.tools.easynetty.service.dm.nettychanels.SenderImpl;
import com.moore.tools.easynetty.service.exchange.NioMessage;
import com.moore.tools.easynetty.service.exchange.send.ISender;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 */
@Slf4j
public class ExampleServerHandler extends BaseAbstractReceiverHandler {
    private final ISender sender;
    /**
     * 存储所有连接的Channel
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public ExampleServerHandler() {
        sender = new SenderImpl();
    }

    @Override
    public void receiveMessage(Channel channel, NioMessage message) {
        log.debug("read:{}", JSON.toJSONString(message));
//        // 读取客户端发送的消息并验证消息序号
//        NettyHelper.receivedData(msg,(s,d)->{
//            NettyHelper.send(ctx.channel(), "", "i got your message :[" + d.replace("\n","") + "]");
//        });
//        String replyMsg = JSON.toJSONString();
        sender.send(channel, new NioMessage(message.getIdentifyId(), "", "i got your message :[" + message.getMessage() + "]"));
    }

    @Override
    public void connected(Channel channel) {
        log.debug("client connected!" + channel.remoteAddress().toString());
//        CHANNELS.add(channel);
        sender.send(channel, new NioMessage(Constant.SERVER_IDENTITY_ID, "", "server connected!"));
    }

    @Override
    public void disconnected(Channel channel) {
//        CHANNELS.remove(channel);
        log.debug("Client disconnected: " + channel.remoteAddress());
    }

    @Override
    public void receiveCompleted(Channel channel) {

    }

    @Override
    public void exception(Channel channel, Throwable cause) {
//        CHANNELS.remove(channel);
        cause.printStackTrace();
    }


}
