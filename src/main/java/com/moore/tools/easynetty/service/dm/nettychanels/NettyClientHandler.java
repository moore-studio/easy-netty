package com.moore.tools.easynetty.service.dm.nettychanels;

import com.moore.tools.easynetty.service.NettyHelper;
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
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 客户端接收到服务端的响应
        NettyHelper.receivedData(msg, (s, data) -> {
            log.info("received response from server: {},s:{}", data, s);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        // 处理异常，一般在这里关闭连接
        ctx.close();
    }
}
