package com.moore.tools.easynetty.service.dm.nettychanels;

import com.moore.tools.easynetty.service.NettyHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:17
 * @description：netty服务端
 * @version: v1
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final Integer uuidLen = UUID.randomUUID().toString().length();
    /**
     * 存储所有连接的Channel
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected!" + ctx.channel().remoteAddress().toString());
        CHANNELS.add(ctx.channel());
        String message = "server connected!";
        NettyHelper.send(ctx.channel(), " ", message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        CHANNELS.remove(ctx.channel());
        log.info("Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        // 读取客户端发送的消息并验证消息序号
        NettyHelper.receivedData(msg, (sequence, data) -> {
            log.info("received message,sequence:{},data:{}", sequence, data);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 处理异常，一般在这里关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
