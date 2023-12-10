package com.moore.tools.easynetty.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * @author ：imoore
 * @date ：created in 2023/12/10 18:05
 * @description：错误消息枚举
 * @version: v
 */
@RequiredArgsConstructor
@Getter
public enum ErrorMessageEnum {
    NO_CHANNEL_HANDLER("E00", "%s is not configured with a ChannelHandler"),
    ;
    private final String code;
    private final String message;

    public String formatter(Object... args) {
        String message = StringUtils.join(Arrays.asList(this.getCode(), this.getMessage()), ":");
        return String.format(message, args);

    }
}
