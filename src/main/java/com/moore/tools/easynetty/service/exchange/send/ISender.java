package com.moore.tools.easynetty.service.exchange.send;

import com.moore.tools.easynetty.service.exchange.NioMessage;
import io.netty.channel.Channel;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 消息发送接口
 *
 * @author ：imoore
 * @date ：created in 2023/12/8 19:45
 * @version: v1.0
 */
public interface ISender {
    /**
     * 消息发送
     *
     * @param channel  信道
     * @param message  消息内容
     */
    void send(Channel channel, NioMessage message);

    /**
     * 添加channel
     *
     * @param channel channel
     */
    void addChannel(Channel channel);

    /**
     * 获取执行器
     * @return ScheduledExecutorService
     */
    ScheduledExecutorService getScheduleExecutorService();
}
