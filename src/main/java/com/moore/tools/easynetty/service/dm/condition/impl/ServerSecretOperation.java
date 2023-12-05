package com.moore.tools.easynetty.service.dm.condition.impl;

import com.moore.commonutil.security.rsa.RSA;
import com.moore.tools.easynetty.entities.ChannelEntity;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.entities.ReplyConfirm;
import com.moore.tools.easynetty.enums.CommandSendType;
import com.moore.tools.easynetty.service.SendHelper;
import com.moore.tools.easynetty.service.dm.server.ServerChannelImpl;
import com.moore.tools.easynetty.service.security.conditions.IOperation;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.security.KeyPair;
import java.util.Objects;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 19:23
 * @description：
 * @version: v
 */
@Slf4j
public class ServerSecretOperation implements IOperation {
    /**
     * 密钥绑定，如果发送的是RSA密钥的情况，需要拿到客户端的私钥（解析客户端数据），再给客户端发送私钥（解析服务端数据）
     *
     * @param channel       信道
     * @param channelEntity 信道实体
     * @param entity        客户端实体
     */
    @Override
    public void execute(ReplyConfirm replyConfirm, Channel channel, ChannelEntity channelEntity, NettyEntity entity) {
        if (StringUtils.isBlank(channelEntity.getClientId())) {
            channelEntity.setClientId(entity.getClientId());
        }
        if (Objects.isNull(channelEntity.getPublicKey())) {
            try {
                channelEntity.setPublicKey(RSA.getPublicKey(entity.getData()));
                log.info("收到客户端公钥信息，绑定成功");
            } catch (Exception e) {
                log.error("get private key error" + e.getMessage(), e);
                return;
            }
        }

        if (Objects.isNull(channelEntity.getPrivateKey())) {
            try {
                KeyPair keyPair = RSA.generateKeyPair();
                channelEntity.setPrivateKey(keyPair.getPrivate());
                entity.setData(RSA.getPublicKeyToPEM(keyPair.getPublic()));
                SendHelper.Server.send(channel, CommandSendType.SECRET_KEY, entity, () -> {
                    ServerChannelImpl.replyConfirms.add(new ReplyConfirm().build(entity));
                });
                log.info("服务与客户端交换密钥中，等待客户端应答");
            } catch (Exception e) {
                log.error("rsa keypair error" + e.getMessage(), e);
            }

        }
    }
}
