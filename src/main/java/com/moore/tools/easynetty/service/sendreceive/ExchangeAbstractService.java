package com.moore.tools.easynetty.service.sendreceive;

import com.alibaba.fastjson.JSON;
import com.moore.commonutil.utils.SystemSecretUtils;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.enums.CommandSendType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author ：imoore
 * @date ：created in 2023/12/4 23:39
 * @description：消息收发抽象类
 * @version: v
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public abstract class ExchangeAbstractService<R> implements IExchangeService {
    private Channel channel;

    @Override
    public void sender(Channel channel, String message) {
        if (!hasChannel(channel) || !hasChannel(this.channel)) {
            log.error("未发送成功：未获取到channel");
            return;
        }
        CompletableFuture.runAsync(() -> {
            internalSender(channel, message);
        });
    }

    /**
     * 消息发送
     *
     * @param message 消息
     */
    @Override
    public void sender(String message) {
        sender(channel, message);
    }

    /**
     * 消息接收
     *
     * @param channel 信道
     * @param message 数据
     * @return 转换实体
     */

    @Override
    public String received(Channel channel, Object message) {
        try {
            String result = "";
            ByteBuf buf = (ByteBuf) message;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            result = new String(bytes);
            // 释放ByteBuf
            buf.release();
            return result;
        } catch (Exception e) {
            log.error("message conversion failed:" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 消息接收
     *
     * @param message 数据
     * @return 转换实体
     */
    @Override
    public String received(Object message) {
        return received(channel, message);
    }

    /**
     * 按实体类型接收
     *
     * @param message      消息
     * @param resultEntity 接结果体类
     * @return 结果
     */
    public R received(Object message, Class<R> resultEntity) {
        String result = Optional.ofNullable(received(channel, message)).orElse("");
        return JSON.parseObject(result, resultEntity);
    }

    /**
     * 发送消息（回调）
     *
     * @param channel  信道
     * @param message  消息
     * @param callback 回调
     */
    public void sendWithCallback(Channel channel, String message, Runnable callback) {
        if (!hasChannel(channel) || !hasChannel(this.channel)) {
            log.error("未发送成功：未获取到channel");
            return;
        }
        if (!hasChannel(channel)) {
            channel = this.channel;
        }
        Channel finalChannel = channel;
        CompletableFuture.runAsync(() -> internalSender(finalChannel, message)).thenRun(callback);
    }

    /**
     * 发送消息（回调）
     *
     * @param message  消息
     * @param callback 回调
     */
    public void sendWithCallback(String message, Runnable callback) {
        sendWithCallback(channel, message, callback);
    }

    @Override
    public <P extends NettyEntity> void sender(CommandSendType type, P message) {
        message.setCommand(new NettyEntity.Command(type));
        message.setFormAddress(SystemSecretUtils.getLocalhostAddress());
        message.setTaskId(message.getCommand().getSequence());
        sender(channel, JSON.toJSONString(message));
    }

    /**
     * 消息收发处理
     *
     * @param channel 信道
     * @param message 消息
     */
    private void internalSender(Channel channel, String message) {
        try {
            message += "\n";
            byte[] bytes = message.getBytes();
            int length = bytes.length;
            ByteBuf buf = channel.alloc().buffer(length);
            // 写入消息内容
            buf.writeBytes(bytes);
            channel.writeAndFlush(buf);
            log.info("internal sender:{}", message);
        } catch (Exception e) {
            log.error("the message failed to send:" + e.getMessage(), e);
        }
    }

    private boolean hasChannel(Channel channel) {
        return Objects.nonNull(channel) && channel.isActive();
    }
}
