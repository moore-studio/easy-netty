package com.moore.tools.easynetty.service.security.conditions;

import com.moore.tools.easynetty.entities.ChannelEntity;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.entities.ReplyConfirm;
import io.netty.channel.Channel;

public interface IOperation {
    /**
     *
     * @param replyConfirm 应答确认：用于server/client发送应答指令时更新应答内容（后续以db形式管理）
     * @param channel 信道
     * @param channelEntity 拿到当前用户Id，发送指令时判断是否要加密 加密用publicKey 解密用privateKey
     *                      每个客户对应两套RAS加密，分别是服务端加解密，客户端加解密，链接创建初期时，客户端要先把本地的rsa的公钥发送到服务端
     *                      服务端用于给客户端加密，服务端将自己生成的rsa的公钥发送给客户端，用于客户端给服务端消息加密
     * @param entity 客户端发送的实体
     */
    void execute(ReplyConfirm replyConfirm, Channel channel, ChannelEntity channelEntity, NettyEntity entity);
}


