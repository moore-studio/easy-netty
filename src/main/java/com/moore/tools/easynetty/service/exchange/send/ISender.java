package com.moore.tools.easynetty.service.exchange.send;

import io.netty.channel.Channel;

/**
 * 消息发送接口
 * @author ：imoore
 * @date ：created in 2023/12/8 19:45
 * @version: v1.0
 */
public interface ISender {
    /**
     * 消息发送
     *
     * @param channel  信道
     * @param sequence 序列，用于确认消息是否发送 可以为null，也可以为""
     * @param message 消息内容
     */
    void send(Channel channel, String sequence, String message);
}
