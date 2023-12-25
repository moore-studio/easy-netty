package com.moore.tools.easynetty.service.exchange;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.service.exchange.send.ISender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消息发送基础实现
 *
 * @author ：imoore
 * @date ：created in 2023/12/8 20:16
 * @version: v
 */
@Slf4j
public abstract class BaseAbstractSender implements ISender {
    protected Queue<NioMessage> messages;
    protected ScheduledExecutorService executorService;
    /**
     * 默认四个字节的预留位置
     */
    private Integer reservedBit = 4;
    protected Channel channel;

    public BaseAbstractSender(Queue<NioMessage> messages, Channel channel) {
        this.messages = messages;
        this.channel = channel;
        executor();
    }

    public BaseAbstractSender() {
        messages = new LinkedBlockingQueue<>(1000);
        executor();
    }

    @Override
    public void addChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * 定时执行发送消息
     * 1线程
     * 每秒执行1次
     */
    public void executor() {
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            NioMessage entity = messages.poll();
            if (entity != null) {
                sendImpl(channel, entity);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    /**
     * 设置消息的预留位置
     * 在 send 方法中，这个预留位用于确保在消息的最开始和结束都有足够的空间存储序列号和消息内容的长度。
     * 在写入sequence和message时分别加了预留位，占了4个字节的空间（默认），这样使接收方能够根据预留位
     * 处存储的胀肚信息准确地解析出来序列号和消息内容
     *
     * @param reservedBit 预留位
     */
    public void setMessageReservedBit(int reservedBit) {
        this.reservedBit = reservedBit;
    }


    @Override
    public void send(Channel channel, String sequence, String message) {
        if (this.channel == null) {
            this.channel = channel;
        }
        addImpl(sequence, message);
    }


    /**
     * 消息发送实现
     *
     * @param channel 信道
     * @param message 消息
     */
    public void sendImpl(Channel channel, NioMessage message) {
        if (!nonChannelInstance()) {
            log.error("未获取到channel");
            return;
        }
        String msg = JSON.toJSONString(message);
        log.debug("send:{}", msg);
        byte[] messageByte = msg.getBytes(StandardCharsets.UTF_8);
        final int messageLen = (int) Math.floor(msg.length() * 1.5) + reservedBit * 2;
        ByteBuf buf = channel.alloc().buffer(messageLen); // Allocate buffer
        buf.writeInt(msg.length());
        buf.writeBytes(messageByte);
        buf.readerIndex(0);
        buf.writerIndex(messageLen);
        channel.writeAndFlush(buf);
    }

    /**
     * chanel未被实例化
     *
     * @return true/false
     */
    public boolean nonChannelInstance() {
        return Objects.nonNull(channel) && channel.isActive();
    }

    /**
     * 消息方式
     *
     * @param sequence 序列号
     * @param message  消息
     */
    public void send(String sequence, String message) {
        addImpl(sequence, message);
    }

    /**
     * 消息添加到队列
     *
     * @param sequence 序列
     * @param message  消息
     */
    public synchronized void addImpl(String sequence, String message) {
        final String sequenceStr = sequence != null ? sequence : "";
        final String messageStr = message != null ? message : "";
        if (StringUtils.isAllBlank(sequenceStr, messageStr)) {
            log.warn("message is empty,not be send");
            return;
        }
        messages.add(new NioMessage(sequenceStr, messageStr));
    }

    /**
     * 获取执行器
     *
     * @return ScheduledExecutorService
     */
    @Override
    public ScheduledExecutorService getScheduleExecutorService() {
        return executorService;
    }
}
