package com.moore.tools.easynetty.service.exchange.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 消息体
 *
 * @author ：imoore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NioMessage implements Serializable {
    private static final Long serialVersionUID = 1L;
    /**
     * 识别Id
     */
    private String identifyId;
    /**
     * 序列 追踪Id
     */
    private String sequence;
    /**
     * 消息体
     */
    private String message;

}
