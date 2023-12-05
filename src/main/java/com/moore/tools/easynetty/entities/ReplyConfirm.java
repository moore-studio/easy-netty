package com.moore.tools.easynetty.entities;

import lombok.Data;

import java.util.Objects;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 20:00
 * @description：应答确认
 * @version: v
 */
@Data
public class ReplyConfirm {
    /**
     * 指令ID (TaskId)
     */
    private String sequence;
    /**
     * 指令内容
     */
    private NettyEntity entity;
    /**
     * 是否应答
     */
    private Boolean isReply;
    /**
     * 是否超时
     */
    private Boolean timeout;
    /**
     * 指令发送时间
     */
    private Long createTimestamp;
    /**
     * 指令应答时间
     */
    private Long replyTimestamp;

    public ReplyConfirm build(NettyEntity entity) {
        setEntity(entity);
        setIsReply(false);
        setTimeout(false);
        setSequence(entity.getTaskId());
        setCreateTimestamp(System.currentTimeMillis());
        return this;
    }

    public boolean isValid() {
        if (Objects.isNull(getReplyTimestamp())) {
            return createTimestamp + (entity.getCommand().getExpired() * 1000) >= System.currentTimeMillis();
        }
        return createTimestamp + (entity.getCommand().getExpired() * 1000) >= replyTimestamp;
    }
}
