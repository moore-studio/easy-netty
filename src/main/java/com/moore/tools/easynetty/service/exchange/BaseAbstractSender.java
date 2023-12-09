package com.moore.tools.easynetty.service.exchange;

import com.moore.tools.easynetty.service.exchange.send.ISender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 消息发送基础实现
 * @author ：imoore
 * @date ：created in 2023/12/8 20:16
 * @version: v
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseAbstractSender implements ISender {
    /**
     * 默认四个字节的预留位置
     */
    private static Integer RESERVED_BIT = 4;
    private Channel channel;

    /**
     * 设置消息的预留位置
     * 在 send 方法中，这个预留位用于确保在消息的最开始和结束都有足够的空间存储序列号和消息内容的长度。
     * 在写入sequence和message时分别加了预留位，占了4个字节的空间（默认），这样使接收方能够根据预留位
     * 处存储的胀肚信息准确地解析出来序列号和消息内容
     *
     * @param reservedBit 预留位
     */
    public void setMessageReservedBit(int reservedBit) {
        RESERVED_BIT = reservedBit;
    }

    @Override
    public void send(Channel channel, String sequence, String message) {
        sendImpl(channel, sequence, message);
    }

    /**
     *  消息发送实现
     * @param channel 信道
     * @param sequence 序列
     * @param message 消息
     */
    public void sendImpl(Channel channel, String sequence, String message) {
        if (nonChannelInstance()) {
            log.error("未获取到channel");
            return;
        }
        log.info("send:{}", message);
        message += "\n";
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        final String sequenceStr = sequence != null ? sequence : "";
        final int sequenceLen = sequenceStr.length();
        ByteBuf buf = channel.alloc().buffer(RESERVED_BIT + sequenceLen + RESERVED_BIT + length); // Allocate buffer
        //序列和数据写入缓冲区
        buf.writeInt(sequenceLen);
        buf.writeCharSequence(sequenceStr, Charset.defaultCharset());
        buf.writeInt(length);
        buf.writeBytes(bytes);
        channel.writeAndFlush(buf);
    }

    /**
     * chanel未被实例化
     * @return true/false
     */
    public boolean nonChannelInstance() {
        return Objects.nonNull(channel) && channel.isActive();
    }

    /**
     * 消息方式
     * @param sequence 序列号
     * @param message 消息
     */

    public void send(String sequence, String message) {

        sendImpl(channel, sequence, message);
    }
}
