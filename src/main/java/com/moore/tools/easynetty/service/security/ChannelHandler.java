package com.moore.tools.easynetty.service.security;

import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.service.SendHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 17:31
 * @description：server处理器
 * @version: v
 */
@Slf4j
public class ChannelHandler extends ChannelInboundHandlerAdapter {
    /**
     * server处理器
     */
    private final IChannelHandler channelHandler;

    public ChannelHandler(IChannelHandler handler) {
        channelHandler = handler;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        channelHandler.active(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        channelHandler.inactive(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyEntity nettyEntity = SendHelper.receive(msg, NettyEntity.class);
        channelHandler.received(ctx.channel(), nettyEntity);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        channelHandler.exceptionCaught(ctx.channel(), cause);
    }
}
