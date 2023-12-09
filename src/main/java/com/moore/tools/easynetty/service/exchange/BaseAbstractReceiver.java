package com.moore.tools.easynetty.service.exchange;

import com.moore.tools.easynetty.service.exchange.receive.IReceiver;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 消息接收基础实现
 * @author ：imoore
 * @date ：created in 2023/12/8 21:49
 * @version: v
 */
@Slf4j
public class BaseAbstractReceiver implements IReceiver<BaseAbstractReceiver.ReceiveEntity<String>> {
    @Override
    public ReceiveEntity<String> receive(Object message) {
        return receiveImpl(message);
    }

    /**
     * 接收消息实体
     *
     * @param message 消息
     * @return 结果
     */
    public ReceiveEntity<String> receiveImpl(Object message) {
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
                    data = new String(content, Charset.defaultCharset()).replace("\n","");
                }
            } else {//读取全部可读数据 进行返回
                content = new byte[buf.readableBytes()];
                buf.readBytes(content);
                data = new String(content, Charset.defaultCharset());
            }
            log.info("Received sequence:{},message:{}", sequence, data);
        } catch (Exception e) {
            log.error("received error " + e.getMessage(), e);
        } finally {
            buf.release();
        }

        return new ReceiveEntity<String>(sequence, data);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiveEntity<R> {
        private String sequence;
        private R data;
    }
}
