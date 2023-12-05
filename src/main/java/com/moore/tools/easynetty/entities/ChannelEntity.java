package com.moore.tools.easynetty.entities;

import io.netty.channel.ChannelId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 17:44
 * @description：
 * @version: v
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelEntity {
    /**
     * channelId
     */
    private ChannelId channelId;
    /**
     * clientId
     */
    private String clientId;
    /**
     * 公钥 双方生成，传给对方，用于数据加密
     */
    private PublicKey publicKey;
    /**
     * 私钥，双方生成，各自保存，用于解密对方的数据
     */
    private PrivateKey privateKey;

    public ChannelEntity build(ChannelId channelId) {
        setChannelId(channelId);
        return this;
    }
}
