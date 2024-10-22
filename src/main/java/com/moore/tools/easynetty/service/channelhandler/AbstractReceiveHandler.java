package com.moore.tools.easynetty.service.channelhandler;

import com.moore.tools.easynetty.common.exceptions.EasyNettyException;
import com.moore.tools.easynetty.service.dm.nettychanels.ReceiverImplHandler;
import com.moore.tools.easynetty.service.exchange.NioMessage;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *
 * @author imoore
 */
@ChannelHandler.Sharable
@Deprecated
public abstract class AbstractReceiveHandler extends ChannelInboundHandlerAdapter implements IReceiver<NioMessage>  {

    protected IReceiver<NioMessage> iReceiver;

    public AbstractReceiveHandler(IReceiver<NioMessage> iReceiver) {
        this.iReceiver = iReceiver;
    }

    public AbstractReceiveHandler() {
        this.iReceiver = new ReceiverImplHandler();
    }


    /**
     * read封装
     * @param ctx channel
     * @param msg 消息
     * @throws EasyNettyException 异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws EasyNettyException {
        try {
            NioMessage receiveEntity = iReceiver.receive(msg);
            receiveMessage(ctx.channel(), receiveEntity.getSequence(), receiveEntity.getMessage());
        } catch (Exception e) {
            throw new EasyNettyException(e);
        }
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

    /**
     * 接收消息
     * @param channel 当前的chanel
     * @param sequence 消息序列号
     * @param data 数据
     */
    public abstract void receiveMessage(Channel channel,String sequence,Object data);
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
