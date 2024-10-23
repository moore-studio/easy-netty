package com.moore.tools.easynetty.service.dm.nettychanels;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.service.exchange.NioMessage;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import com.moore.tools.easynetty.service.exchange.send.ISender;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:17
 * @description：netty服务端
 * @version: v1
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final IReceiver<NioMessage> receiver;
    private final ISender sender;

    public NettyServerHandler() {
        receiver = new ReceiverImplHandler("1");
        sender = new SenderImpl();

    }

    /**
     * 存储所有连接的Channel
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client connected!" + ctx.channel().remoteAddress().toString());
        CHANNELS.add(ctx.channel());
        sender.send(ctx.channel(), new NioMessage("","","server connected!"));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        CHANNELS.remove(ctx.channel());
        log.debug("Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("read:");
//        // 读取客户端发送的消息并验证消息序号
//        NettyHelper.receivedData(msg,(s,d)->{
//            NettyHelper.send(ctx.channel(), "", "i got your message :[" + d.replace("\n","") + "]");
//        });
        NioMessage entity = receiver.receive(msg);
        sender.send(ctx.channel(), new NioMessage(entity.getIdentifyId(),"", "i got your message :[" + entity.getMessage() + "]"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 处理异常，一般在这里关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
