package com.moore.tools.easynetty.service.channelhandler;

import com.moore.tools.easynetty.common.exceptions.EasyNettyException;
import com.moore.tools.easynetty.service.exchange.entity.NioMessage;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * server处理器
 *
 * @author ：imoore
 */
@ChannelHandler.Sharable
@Deprecated
public abstract class ServerAbstractChannelHandler extends ChannelInboundHandlerAdapter {
    protected IReceiver<NioMessage> receiver;

    public ServerAbstractChannelHandler(IReceiver<NioMessage> receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            connected(ctx.channel());
        } catch (Exception e) {
            throw new EasyNettyException(e);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            disconnected(ctx.channel());
        } catch (Exception e) {
            throw new EasyNettyException(e);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            NioMessage receiveEntity = receiver.receive(msg);
            receive(ctx.channel(), receiveEntity.getSequence(), receiveEntity.getMessage());
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            exception(ctx.channel(), cause);
        } catch (Exception e) {
            throw new EasyNettyException(e);
        }
    }


    /**
     * 连接创建
     *
     * @param channel 信道
     */
    public abstract void connected(Channel channel);

    /**
     * 客户端断连
     *
     * @param channel 信道
     */
    public abstract void disconnected(Channel channel);

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

    /**
     * 一场捕获
     *
     * @param channel 信道
     * @param cause   错误发生原因
     */
    public abstract void exception(Channel channel, Throwable cause);
}
