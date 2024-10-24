package com.moore.tools.easynetty.service.dm.nettychanels;

import com.moore.tools.easynetty.service.exchange.entity.NioMessage;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 * @date ：created in 2023/11/14 23:59
 * @description：nettychanel客户端
 * @version: v1
 */

@Slf4j
@ChannelHandler.Sharable
@Deprecated
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final IReceiver<NioMessage> receiver;

    public NettyClientHandler() {
        receiver = new ReceiverImplHandler("1");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        receiver.receive(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
        ctx.close();
    }
}
