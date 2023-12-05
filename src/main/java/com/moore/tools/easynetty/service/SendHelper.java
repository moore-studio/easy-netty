package com.moore.tools.easynetty.service;


import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.enums.CommandSendType;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 13:29
 * @description：消息发送
 * @version: v
 */
@Slf4j
public class SendHelper {
    public static <R extends NettyEntity> R receive(Object data, Class<R> entity) {
        AtomicReference<R> received = new AtomicReference<>();
        NettyHelper.receivedData(data, msg -> {
            received.set(JSON.parseObject(msg, entity));
        });
        return received.get();
    }

    public static class Server {
        /**
         * server。send 指令发送完毕后，将指令内容放到应答确认中
         *
         * @param channel         信道
         * @param commandSendType 指令类型
         * @param entity          发送实体
         * @param <P>
         */
        public static <P extends NettyEntity> void send(Channel channel, CommandSendType commandSendType, P entity,Runnable callback) {
            SendHelper.send(channel, commandSendType, entity, callback);
        }

        /**
         * server。send 指令发送完毕后
         *
         * @param channel         信道
         * @param commandSendType 指令类型
         * @param entity          发送实体
         * @param <P>
         */
        public static <P extends NettyEntity> void send(Channel channel, CommandSendType commandSendType, P entity) {
            SendHelper.send(channel, commandSendType, entity, () -> {
            });
        }
    }

    public static class Client {
        /**
         * server。send 指令发送完毕后，将指令内容放到应答确认中
         *
         * @param channel         信道
         * @param commandSendType 指令类型
         * @param entity          发送实体
         * @param callback        回调
         * @param <P>
         */
        public static <P extends NettyEntity> void send(Channel channel, CommandSendType commandSendType, P entity, Runnable callback) {
            SendHelper.send(channel, commandSendType, entity, callback);
        }

        /**
         * server。send 指令发送完毕后
         *
         * @param channel         信道
         * @param commandSendType 指令类型
         * @param entity          发送实体
         * @param <P>
         */
        public static <P extends NettyEntity> void send(Channel channel, CommandSendType commandSendType, P entity) {
            SendHelper.send(channel, commandSendType, entity, () -> {
            });
        }
    }

    public static <P extends NettyEntity> void send(Channel channel, CommandSendType type, P entity, Runnable callBack) {
        entity.setCommand(new NettyEntity.Command(type));
        entity.setTaskId(entity.getCommand().getSequence());
        String message = JSON.toJSONString(entity);
        log.info("send:{}", message);
        CompletableFuture.runAsync(() -> NettyHelper.send(channel, message)).thenRun(callBack);
    }
}
