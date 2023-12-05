package com.moore.tools.easynetty.service.dm.condition.impl;

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
 * @date ：created in 2023/12/2 20:22
 * @description：应答确认
 * @version: v
 */
@Slf4j
public class ReplyConfirmOperation implements IOperation {
    @Override
    public void execute(ReplyConfirm replyConfirm, Channel channel, ChannelEntity channelEntity, NettyEntity entity) {
        if (Objects.isNull(replyConfirm)) {
            log.warn("reply:未找到需要确认的应答");
            return;
        }
        replyConfirm.setReplyTimestamp(System.currentTimeMillis());
        replyConfirm.setIsReply(true);
        replyConfirm.setTimeout(false);
        if (!replyConfirm.isValid()) {
            log.warn("当前指令已过期");
            replyConfirm.setTimeout(true);
        }
        String data = null;
        if (StringUtils.isNotBlank(entity.getData())) {
            if (entity.getCommand().isEncrypt()) {
                data = RSA.decrypt(channelEntity.getPrivateKey(), entity.getData());
            } else {
                data = entity.getData();
            }
        }
        log.info("已收到客户端应答，指令：{}，是否超时：{}，是否有效：{}，应答内容：{}", replyConfirm.getEntity().getCommand().getCommand(), replyConfirm.getTimeout(), replyConfirm.isValid(), data);


    }
}
