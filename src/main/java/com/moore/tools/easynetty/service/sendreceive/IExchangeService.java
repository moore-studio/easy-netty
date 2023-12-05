package com.moore.tools.easynetty.service.sendreceive;

import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.enums.CommandSendType;
import com.moore.tools.easynetty.process.NettyClient;
import io.netty.channel.Channel;

import java.util.function.Consumer;

/**
 * @author ：imoore
 * @date ：created in 2023/12/4 23:32
 * @description：消息收发服务
 * @version: v
 */
public interface IExchangeService {
    /**
     * 消息发送
     * @param channel 信道
     * @param message 数据
     */
    void sender(Channel channel,String message);
    /**
     * 消息发送
     * @param message 数据
     */
    void sender(String message);

    /**
     * 消息发送
     * @param type 类型
     * @param message 消息
     */
    <P extends NettyEntity> void sender(CommandSendType type, P message);

    /**
     * 消息接收
     * @param channel 信道
     * @param message 数据
     * @return entity
     */
    String received(Channel channel,Object message);
    /**
     * 消息接收
     * @param message 数据
     * @return entity
     */
    String received(Object message);

}
