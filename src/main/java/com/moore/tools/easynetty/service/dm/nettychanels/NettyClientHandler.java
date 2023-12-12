package com.moore.tools.easynetty.service.dm.nettychanels;

import com.moore.tools.easynetty.service.exchange.BaseAbstractReceiver;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
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

    private final IReceiver<BaseAbstractReceiver.ReceiveEntity<String>> receiver;

    public NettyClientHandler() {
        receiver = new ReceiverImpl();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        receiver.receive(msg);
//        NettyHelper.receivedData(msg, (s, s1) -> {
//            log.info("s1:{},s:{}", s, s1);
//        });
        // 客户端接收到服务端的响应
//        NettyHelper.receivedData(msg, (s, data) -> {
//            //log.debug("received response from server: {},s:{}", data);
//        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        // 处理异常，一般在这里关闭连接
        ctx.close();
    }
}
