package com.moore.tools.easynetty.zexample;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.common.constants.Constant;
import com.moore.tools.easynetty.service.channelhandler.BaseAbstractReceiverHandler;
import com.moore.tools.easynetty.service.dm.nettychanels.SenderImpl;
import com.moore.tools.easynetty.service.exchange.NioMessage;
import com.moore.tools.easynetty.service.exchange.send.ISender;
import com.sun.xml.internal.ws.api.ha.StickyFeature;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：imoore
 */
@Slf4j
public class ExampleServerHandler extends BaseAbstractReceiverHandler {
    /**
     * 存储所有连接的Channel
     */
//    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ConcurrentHashMap<String, Channel> user = new ConcurrentHashMap<>();
    private static List<Channel> ACTIVE_CHANNELS = new ArrayList<>();

    public ExampleServerHandler(String identityId) {
        super(identityId);
    }

    @Override
    public void receiveMessage(ChannelHandlerContext channel, NioMessage message) {
        if("INIT_CLIENT_IDENTIFY_ID".equals(message.getMessage())){
            if(!channel.channel().isActive()){
                log.warn("Current inactive channel");
                return;
            }
        Channel oldChannel = user.get(message.getIdentifyId());

        if (oldChannel != null && oldChannel != channel) {
            // 如果已存在旧的Channel且不是当前的Channel，则关闭旧的连接
            log.warn("close old channel");
            oldChannel.close();
        }
            user.put(message.getIdentifyId(),channel.channel());
            channel.channel().attr(Constant.ATTR_IDENTIFY_ID).set(message.getIdentifyId());
        }
        sender.send(user.get(message.getIdentifyId()), new NioMessage(identityId, "", "i got your message :[" + message.getMessage() + "]"));
    }

    @Override
    public void connected(ChannelHandlerContext channel) {
//        ACTIVE_CHANNELS.add(channel.channel());
//        log.debug("client connected!" + channel.channel().remoteAddress().toString());
//        String clientId = getChanelIdentityId(channel.channel()); // 自定义获取客户端标识的方法
//        log.info("server get client identify id: {}", clientId);
//        Channel oldChannel = user.get(clientId);
//
//        if (oldChannel != null && oldChannel != channel) {
//            // 如果已存在旧的Channel且不是当前的Channel，则关闭旧的连接
//            log.warn("close old channel");
//            oldChannel.close();
//        }
//
//        // 保存或替换新的Channel
//        user.put(clientId, channel.channel());
//
//        super.channelActive(channel);
//        CHANNELS.add(channel);
        sender.send(channel.channel(), new NioMessage(Constant.SERVER_IDENTITY_ID, "", "server connected!"));
    }

    @Override
    public void disconnected(ChannelHandlerContext channel) {
//        CHANNELS.remove(channel);
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
