package com.moore.tools.easynetty.service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author imoore
 * @date 2023/11/13
 */
@Slf4j
public class NettyHelper {

    /**
     * 保留字节
     */
    private static final Integer RESERVED_BIT = 4;

    /**
     * 发送消息和序列（UUID）
     *
     * @param channel
     * @param message
     */
    public static void send(Channel channel, String message) {
        send(channel, "", message);
    }

    /**
     * 发送消息
     *
     * @param channel
     * @param message
     */
    public static void send(Channel channel, String sequence, String message) {
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
     * 接收消息
     *
     * @param message
     * @param messageConsumer
     */
    public static void receivedData(Object message, Consumer<String> messageConsumer) {
        receivedData(message, (sequence, msg) -> messageConsumer.accept(msg));
    }

    /**
     * 接收消息&序列（自定义）
     *
     * @param message
     * @param messageConsumer
     */
    public static void receivedData(Object message, BiConsumer<String, String> messageConsumer) {
        ByteBuf buf = (ByteBuf) message;
        try {
            int sequenceLen = 0;
            int contentLen = 0;

            //检查是否有足够的可读字节 最少8bit
            if (buf.readableBytes() >= 8) {
                sequenceLen = buf.readInt();
                contentLen = buf.readInt();
            }

            String sequence = "";
            String data = "";
            byte[] content;
            //是否有足够的数据来读取序列号和内容
            if (buf.readableBytes() >= sequenceLen + contentLen) {
                //序列是否存在
                if (sequenceLen > 0) {
                    CharSequence receivedSequence = buf.readCharSequence(sequenceLen, Charset.defaultCharset()); // Read sequence
                    content = new byte[contentLen];
                    buf.readBytes(content); // Read content
                    sequence = receivedSequence.toString();
                    data = new String(content, Charset.defaultCharset());
                } else { //序列不存在
                    content = new byte[contentLen];
                    buf.readBytes(content); // Read content
                    data = new String(content, Charset.defaultCharset());
                }
            } else {//读取全部可读数据 进行返回
                content = new byte[buf.readableBytes()];
                buf.readBytes(content);
                data = new String(content, Charset.defaultCharset());
            }
            messageConsumer.accept(sequence, data);
        } catch (Exception e) {
            log.error("received error " + e.getMessage(), e);
        } finally {
            buf.release();
        }
    }


}
