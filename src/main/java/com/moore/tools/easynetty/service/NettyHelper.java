package com.moore.tools.easynetty.service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author imoore
 * @date 2023/11/13
 */
public class NettyHelper {

    private final static Integer UUID_LEN = UUID.randomUUID().toString().length();

    /**
     * 发送消息和序列（UUID）
     * @param channel
     * @param message
     */
    public static void sendWithSequence(Channel channel, String message) {
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        String uuid = UUID.randomUUID().toString();
        ByteBuf buf = channel.alloc().buffer(uuid.length() + length);
        // 写入消息序号
        buf.writeCharSequence(uuid, Charset.defaultCharset());
        // 写入消息长度
        buf.writeInt(length);

        // 写入消息内容
        buf.writeBytes(bytes);
        channel.writeAndFlush(buf);
    }

    /**
     * 发送消息
     * @param channel
     * @param message
     */
    public static void send(Channel channel, String message) {
        message += "\n";
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        ByteBuf buf = channel.alloc().buffer(length);
        // 写入消息内容
        buf.writeBytes(bytes);
        channel.writeAndFlush(buf);
    }

    /**
     * 接收消息
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
    }

    /**
     * 接收消息&序列（自定义）
     * @param message
     * @param sequenceLen
     * @param messageConsumer
     */
    public static void receivedDataWithSequence(Object message, Integer sequenceLen, BiConsumer<String, String> messageConsumer) {
        // 读取客户端发送的消息并验证消息序号
        ByteBuf buf = (ByteBuf) message;
        try {
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

    /**
     * 接收消息以及序列（UUID）
     * @param message
     * @param messageConsumer
     */
    public static void receivedDataWithSequence(Object message, BiConsumer<String, String> messageConsumer) {
        receivedDataWithSequence(message, UUID_LEN, messageConsumer);
    }


}
