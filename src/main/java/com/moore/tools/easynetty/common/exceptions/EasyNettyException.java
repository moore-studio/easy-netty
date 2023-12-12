package com.moore.tools.easynetty.common.exceptions;

import com.moore.tools.easynetty.common.enums.ErrorMessageEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author ：imoore
 * @date ：created in 2023/12/10 17:55
 * @description：exception
 * @version: v
 */
@Getter
@Slf4j
public class EasyNettyException extends RuntimeException {
    private String code;
    private String message;

    public EasyNettyException(String message) {
        super(message);
        this.message = message;
    }

    public EasyNettyException(String code, String message) {
        super(messageFormat(code, message));
        this.code = code;
        this.message = message;
    }

    public EasyNettyException(ErrorMessageEnum error) {
        super(messageFormat(error.getCode(), error.getMessage()));
        this.code = error.getCode();
        this.message = error.getMessage();
    }

    public EasyNettyException(String code, String message, Throwable cause) {
        super(messageFormat(code, message), cause);
    }

    public EasyNettyException(Throwable cause) {
        super(cause);
    }

    private static String messageFormat(String code, String message) {
        log.error("error code:{},message:{}", code, message);
        return StringUtils.join(Arrays.asList(code, message), ":");
    }
}
