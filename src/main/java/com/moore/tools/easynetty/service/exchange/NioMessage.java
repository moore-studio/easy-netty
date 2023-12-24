package com.moore.tools.easynetty.service.exchange;

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
    private String sequence;
    private String message;

}
