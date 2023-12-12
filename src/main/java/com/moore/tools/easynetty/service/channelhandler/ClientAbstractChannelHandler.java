package com.moore.tools.easynetty.service.channelhandler;

import com.moore.tools.easynetty.common.exceptions.EasyNettyException;
import com.moore.tools.easynetty.service.exchange.BaseAbstractReceiver;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 客户端处理器
 *
 * @author ：imoore
 */
public abstract class ClientAbstractChannelHandler extends ChannelInboundHandlerAdapter {
    protected IReceiver<BaseAbstractReceiver.ReceiveEntity<String>> receiver;

    public ClientAbstractChannelHandler(IReceiver<BaseAbstractReceiver.ReceiveEntity<String>> receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            BaseAbstractReceiver.ReceiveEntity<String> receiveEntity = receiver.receive(msg);
            receive(ctx.channel(), receiveEntity.getSequence(), receiveEntity.getData());
        } catch (Exception e) {
            throw new EasyNettyException(e);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        try {
            receiveCompleted(ctx.channel());
        } catch (Exception e) {
            throw new EasyNettyException(e);
        }
    }

    /**
     * 消息接收
     *
     * @param channel  信道
     * @param sequence 序列号
     * @param message  消息
     */
    public abstract void receive(Channel channel, String sequence, String message);

    /**
     * 消息接收完成
     *
     * @param channel 信道
     */
    public abstract void receiveCompleted(Channel channel);
}
