package com.moore.tools.easynetty.service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author imoore
 * @date 2023/11/13
 */
@Slf4j
public class NettyHelper {

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
        message += "\n";
        byte[] bytes = message.getBytes();
        int length = bytes.length;

        final String sequenceStr = Optional.ofNullable(sequence).orElse("");
        final int sequenceLen = sequenceStr.length();
        ByteBuf buf = channel.alloc().buffer(sequenceLen + length);
        // 写入消息长度序列
        buf.writeInt(sequenceLen);
        // 写入消息序号
        buf.writeCharSequence(sequenceStr, Charset.defaultCharset());
        // 写入消息长度
        buf.writeInt(length);

        // 写入消息内容
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
        ByteBuf buf = (ByteBuf) message;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        messageConsumer.accept(new String(bytes));
        // 释放ByteBuf
        buf.release();
//        receivedData(message,(sequence,msg)->messageConsumer.accept(msg));
    }

    /**
     * 接收消息&序列（自定义）
     *
     * @param message
     * @param messageConsumer
     */
    public static void receivedData(Object message, BiConsumer<String, String> messageConsumer) {
        // 读取客户端发送的消息并验证消息序号
        ByteBuf buf = (ByteBuf) message;
        try {
            // 读取序列号长度
            int sequenceLen = buf.readInt();
            // 读取消息序号
            CharSequence receivedSequence = buf.readCharSequence(sequenceLen, Charset.defaultCharset());
            // 读取消息长度
            int length = buf.readInt();
            byte[] content = new byte[length];
            buf.readBytes(content);
            messageConsumer.accept(receivedSequence.toString(), new String(content));
        } finally {
            buf.release(); // 释放ByteBuf
        }
    }


}
