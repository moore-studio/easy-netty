package com.moore.tools.easynetty.zexample;

import com.moore.tools.easynetty.common.constants.Constant;
import com.moore.tools.easynetty.service.channelhandler.BaseAbstractReceiverHandler;
import com.moore.tools.easynetty.service.exchange.entity.NioMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：imoore
 */
@Slf4j
public class ExampleServerHandler extends BaseAbstractReceiverHandler {
    /**
     * 存储所有连接的Channel
     */

    private static final ConcurrentHashMap<String, Channel> user = new ConcurrentHashMap<>();
    public ExampleServerHandler(String identityId) {
        super(identityId);
    }

    @Override
    public void receiveMessage(ChannelHandlerContext channel, NioMessage message) {
        if ("INIT_CLIENT_IDENTIFY_ID".equals(message.getMessage())) {
            if (!channel.channel().isActive()) {
                log.warn("Current inactive channel");
                return;
            }
            Channel oldChannel = user.get(message.getIdentifyId());

            if (oldChannel != null && oldChannel != channel) {
                // 如果已存在旧的Channel且不是当前的Channel，则关闭旧的连接
                log.warn("close old channel");
                oldChannel.close();
            }
            user.put(message.getIdentifyId(), channel.channel());
            channel.channel().attr(Constant.ATTR_IDENTIFY_ID).set(message.getIdentifyId());
        }
        sender.send(user.get(message.getIdentifyId()), new NioMessage(identityId, "", "i got your message :[" + message.getMessage() + "]"));
    }

    @Override
    public void connected(ChannelHandlerContext channel) {
        sender.send(channel.channel(), new NioMessage(identityId, "", "server connected!"));
    }

    @Override
    public void disconnected(ChannelHandlerContext channel) {
        log.debug("Client disconnected: " + channel.channel().remoteAddress());
    }

    @Override
    public void receiveCompleted(ChannelHandlerContext channel) {

    }

    @Override
    public void exception(ChannelHandlerContext channel, Throwable cause) {
//        CHANNELS.remove(channel);
        cause.printStackTrace();
    }


}
