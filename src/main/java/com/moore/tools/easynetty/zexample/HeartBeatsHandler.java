package com.moore.tools.easynetty.zexample;

import com.moore.tools.easynetty.service.dm.nettychanels.SenderImpl;
import com.moore.tools.easynetty.service.exchange.send.ISender;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.UUID;

/**
 * @author imoore
 */
@ChannelHandler.Sharable
public class HeartBeatsHandler extends ChannelInboundHandlerAdapter {
    ISender sender;
    public HeartBeatsHandler() {
        sender = new SenderImpl();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state() == IdleStateEvent.READER_IDLE_STATE_EVENT.state()) {
//                System.out.println("读超时，关闭连接");
//                ctx.close();  // 读超时后关闭连接
//            }
//        } else {
//            super.userEventTriggered(ctx, evt);
//        }
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    System.out.println("Read timeout: No data received for a while.");
                    // 你可以选择关闭连接或者发送心跳包
                    ctx.close();  // 超时关闭连接
                    break;
                case WRITER_IDLE:
                    System.out.println("Write timeout: No data sent for a while.");
                    // 可以选择发送心跳包到客户端
                    sender.send(ctx.channel(), UUID.randomUUID().toString(), "HEART_BEATS");
                    break;
                case ALL_IDLE:
                    System.out.println("All timeout: No read or write for a while.");
                    // 根据需要处理 ALL_IDLE 状态
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);  // 如果不是心跳事件，则调用父类方法
        }
    }
}
