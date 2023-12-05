package com.moore.tools.easynetty.service.dm.condition.impl;

import com.moore.commonutil.security.rsa.RSA;
import com.moore.tools.easynetty.entities.ChannelEntity;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.entities.ReplyConfirm;
import com.moore.tools.easynetty.service.security.conditions.IOperation;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author ：imoore
 * @date ：created in 2023/12/3 1:28
 * @description：客户端获取密钥
 * @version: v
 */
@Slf4j
public class ClientSecretOperation implements IOperation {
    @Override
    public void execute(ReplyConfirm replyConfirm, Channel channel, ChannelEntity channelEntity, NettyEntity entity) {
        log.info("client secret operation start");
        if(Objects.isNull(channelEntity.getPublicKey())){
            channelEntity.setPublicKey(RSA.getPublicKey(entity.getData()));
            log.info("ras 公钥获取成功");
        }
    }
}
