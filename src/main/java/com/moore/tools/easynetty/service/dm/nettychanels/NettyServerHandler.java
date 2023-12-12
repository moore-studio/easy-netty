package com.moore.tools.easynetty.service.dm.nettychanels;

import com.moore.tools.easynetty.service.NettyHelper;
import com.moore.tools.easynetty.service.exchange.BaseAbstractReceiver;
import com.moore.tools.easynetty.service.exchange.BaseAbstractSender;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import com.moore.tools.easynetty.service.exchange.send.ISender;
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
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final IReceiver<BaseAbstractReceiver.ReceiveEntity<String>> receiver;
    private final ISender sender;

    public NettyServerHandler() {
        receiver = new ReceiverImpl();
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
        String message = "server connected!";
        NettyHelper.send(ctx.channel(), "", message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        CHANNELS.remove(ctx.channel());
        log.debug("Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        // 读取客户端发送的消息并验证消息序号
//        NettyHelper.receivedData(msg,(s,d)->{
//            NettyHelper.send(ctx.channel(), "", "i got your message :[" + d.replace("\n","") + "]");
//        });
        BaseAbstractReceiver.ReceiveEntity<String> entity = receiver.receive(msg);

        sender.send(ctx.channel(), "", "i got your message :[" + entity.getData() + "]");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 处理异常，一般在这里关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
