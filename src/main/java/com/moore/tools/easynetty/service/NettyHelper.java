package com.moore.tools.easynetty.service;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.enums.CommandSendType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author imoore
 * @date 2023/11/13
 */
@Slf4j
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

    /**
     * 接收消息（根据NettyEntity）
     * @param data  JSON NettyEntity
     * @param entity NettyEntity
     * @return NettyEntity
     * @param <R> NettyEntity
     */
    public static <R extends NettyEntity> R receive(Object data, Class<R> entity) {
        AtomicReference<R> received = new AtomicReference<>();
        receivedData(data, msg -> {
            received.set(JSON.parseObject(msg, entity));
        });
        return received.get();
    }

    /**
     * 指令发送：按照NettyEntity实体发送消息
     * @param channel
     * @param type
     * @param entity
     * @param callBack
     * @param <P>
     */
    public static <P extends NettyEntity> void send(Channel channel, CommandSendType type, P entity, Runnable callBack) {
        entity.setCommand(new NettyEntity.Command(type));
        entity.setTaskId(entity.getCommand().getSequence());
        String message = JSON.toJSONString(entity);
        log.info("send:{}", message);
        CompletableFuture.runAsync(() -> NettyHelper.send(channel, message)).thenRun(callBack);
    }
}
