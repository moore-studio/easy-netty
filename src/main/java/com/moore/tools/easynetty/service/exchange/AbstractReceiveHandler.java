package com.moore.tools.easynetty.service.exchange;

import com.moore.tools.easynetty.common.exceptions.EasyNettyException;
import com.moore.tools.easynetty.service.dm.nettychanels.ReceiverImpl;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *
 * @author imoore
 */
public abstract class AbstractReceiveHandler extends ChannelInboundHandlerAdapter implements IReceiver<NioMessage>  {

    protected IReceiver<NioMessage> iReceiver;

    public AbstractReceiveHandler(IReceiver<NioMessage> iReceiver) {
        this.iReceiver = iReceiver;
    }

    public AbstractReceiveHandler() {
        this.iReceiver = new ReceiverImpl();
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws EasyNettyException {
        try {
            NioMessage receiveEntity = iReceiver.receive(msg);
            receiveMessage(ctx.channel(), receiveEntity.getSequence(), receiveEntity.getMessage());
        } catch (Exception e) {
            throw new EasyNettyException(e);
        }
    }

    public abstract void receiveMessage(Channel channel,String sequence,Object data);
}
