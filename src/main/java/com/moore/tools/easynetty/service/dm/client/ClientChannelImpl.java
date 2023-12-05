package com.moore.tools.easynetty.service.dm.client;

import com.moore.commonutil.security.rsa.RSA;
import com.moore.tools.easynetty.entities.ChannelEntity;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.entities.ReplyConfirm;
import com.moore.tools.easynetty.enums.CommandSendType;
import com.moore.tools.easynetty.service.security.IChannelHandler;
import com.moore.tools.easynetty.service.SendHelper;
import com.moore.tools.easynetty.service.security.conditions.IOperation;
import com.moore.tools.easynetty.service.dm.condition.impl.ClientSecretOperation;
import com.moore.tools.easynetty.service.dm.condition.impl.DefaultOperation;
import com.moore.tools.easynetty.service.dm.condition.impl.ReplyConfirmOperation;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;
import java.util.*;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 20:41
 * @description：客户端
 * @version: v
 */
@Slf4j
public class ClientChannelImpl implements IChannelHandler {
    public List<ReplyConfirm> replyConfirms = new ArrayList<>();
    private Map<String, IOperation> condition = null;
    public static final String CLIENT_ID = UUID.randomUUID().toString();
    public static ChannelEntity channelEntity = new ChannelEntity();

    public ClientChannelImpl() {
        condition = new HashMap<>(CommandSendType.values().length);
        condition.put(CommandSendType.SECRET_KEY.getCommand(), new ClientSecretOperation());
        condition.put(CommandSendType.REPLY.getCommand(), new ReplyConfirmOperation());
        condition.put(CommandSendType.REPLY_SECRET.getCommand(), new ReplyConfirmOperation());

    }

    @Override
    public void active(Channel channel) {
        KeyPair keyPair = null;
        try {
            keyPair = RSA.generateKeyPair();
        } catch (Exception e) {
            log.warn("client error rsa" + e.getMessage(), e);
            return;
        }

        NettyEntity nettyEntity = new NettyEntity();
        nettyEntity.setClientId(CLIENT_ID);
        nettyEntity.setFormAddress("127.0.0.1");
        nettyEntity.setData(RSA.getPublicKeyToPEM(keyPair.getPublic()));
        channelEntity.build(channel.id());
        channelEntity.setPrivateKey(keyPair.getPrivate());
        channelEntity.setClientId(nettyEntity.getClientId());


        SendHelper.Client.send(channel, CommandSendType.SECRET_KEY, nettyEntity, () -> replyConfirms.add(new ReplyConfirm().build(nettyEntity)));
    }

    @Override
    public void inactive(Channel channel) {

    }

    @Override
    public <R extends NettyEntity> void received(Channel channel, R entity) {
        IOperation operation = Optional.ofNullable(condition.get(entity.getCommand().getCommand())).orElse(new DefaultOperation());

        ReplyConfirm replyConfirm = replyConfirms.stream()
                .filter(r -> entity.getCommand().getSequence().equalsIgnoreCase(r.getSequence()))
                .findFirst().orElse(null);
        operation.execute(replyConfirm, channel, channelEntity, entity);

        SendHelper.Client.send(channel, CommandSendType.REPLY_SECRET, entity, () -> replyConfirms.add(new ReplyConfirm().build(entity)));
    }

    @Override
    public void exceptionCaught(Channel channel, Throwable throwable) {

    }
}
