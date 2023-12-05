package com.moore.tools.easynetty.service.dm.condition.impl;

import com.alibaba.fastjson2.JSON;
import com.moore.commonutil.security.rsa.RSA;
import com.moore.tools.easynetty.entities.ChannelEntity;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.entities.ReplyConfirm;
import com.moore.tools.easynetty.service.security.conditions.IOperation;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 19:44
 * @description：默认处理方式
 * @version: v
 */
@Slf4j
public class DefaultOperation implements IOperation {
    @Override
    public void execute(ReplyConfirm replyConfirm, Channel channel, ChannelEntity channelEntity, NettyEntity entity) {
        log.info("default operation received:{}", JSON.toJSONString(entity));
        if (entity.getCommand().isEncrypt() && Objects.nonNull(channelEntity.getPrivateKey()) && StringUtils.isNotBlank(entity.getData())) {
            log.info("decrypt:{}", RSA.decrypt(channelEntity.getPrivateKey(), entity.getData()));
        }
    }
}
