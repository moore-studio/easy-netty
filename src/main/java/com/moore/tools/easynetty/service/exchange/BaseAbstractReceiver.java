package com.moore.tools.easynetty.service.exchange;

import com.alibaba.fastjson.JSON;
import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 消息接收基础实现
 *
 * @author ：imoore
 * @date ：created in 2023/12/8 21:49
 * @version: v
 */
@Slf4j
public abstract class BaseAbstractReceiver implements IReceiver<NioMessage> {
    @Override
    public NioMessage receive(Object message) {
        return receiveImpl(message);
    }

    /**
     * 接收消息实体
     *
     * @param message 消息
     * @return 结果
     */
    @Deprecated
    public ReceiveEntity<String> receiveImp(Object message) {
        String sequence = "";
        String data = "";
        ByteBuf buf = (ByteBuf) message;
        try {
            int sequenceLen = 0;
            int contentLen = 0;

            //检查是否有足够的可读字节 最少8bit
            if (buf.readableBytes() >= 8) {
                sequenceLen = buf.readInt();
                contentLen = buf.readInt();
            }
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
                    data = new String(content, Charset.defaultCharset()).replace("\n", "");
                }
            } else {//读取全部可读数据 进行返回
                content = new byte[buf.readableBytes()];
                buf.readBytes(content);
                data = new String(content, Charset.defaultCharset());
            }
            log.debug("Received sequence:{},message:{}", sequence, data);
        } catch (Exception e) {
            log.error("received error " + e.getMessage(), e);
        } finally {
            buf.release();
        }

        return new ReceiveEntity<String>(sequence, data);
    }

    public NioMessage receiveImpl(Object message) {
        ByteBuf buf = (ByteBuf) message;
        NioMessage msg = null;
        try {
            int len = buf.readInt();
            byte[] messageByte = new byte[len];
            buf.readBytes(messageByte);
            String data = new String(messageByte, StandardCharsets.UTF_8);
            msg = JSON.parseObject(data, NioMessage.class);
            log.debug("Received:{}", data);
        } catch (Exception e) {
            log.error("received error " + e.getMessage(), e);
        } finally {
            buf.release();
        }
        return msg;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiveEntity<R> {
        private String sequence;
        private R data;
    }
}
