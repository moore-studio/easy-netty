package com.moore.tools.easynetty.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * sendHelper.Server
 */
@Getter
@RequiredArgsConstructor
public enum CommandSendType {
    /**
     * 发送密钥
     */
    SECRET_KEY(UUID.randomUUID().toString(), "SECRET_KEY", 30, "INTERNAL", false),
    /**
     * 查询/获取
     */
    GET(UUID.randomUUID().toString(), "GET", 30, "INTERNAL", true),
    /**
     * 上传
     */
    PUT(UUID.randomUUID().toString(), "PUT", 30, "INTERNAL", true),
    /**
     * 删除
     */
    DELETE(UUID.randomUUID().toString(), "DELETE", 30, "INTERNAL", true),
    /**
     * 更新
     */
    UPDATE(UUID.randomUUID().toString(), "UPDATE", 30, "INTERNAL", true),
    /**
     * 其他
     */
    OTHER(UUID.randomUUID().toString(), "OTHER", 30, "INTERNAL", false),
    REPLY(UUID.randomUUID().toString(), "REPLY", 30, "INTERNAL", true),
    REPLY_SECRET(UUID.randomUUID().toString(), "REPLY_SECRET", 30, "INTERNAL", false),
    ;
    /**
     * 序列
     */
    private final String sequence;
    /**
     * 指令
     */
    private final String command;
    /**
     * 过期时间
     */
    private final Integer expire;
    /**
     * 指令类型 SHELL/INTERNAL/OTHER
     */
    private final String type;
    /**
     * 当前指令是否被加密
     */
    private final Boolean encrypt;

}
