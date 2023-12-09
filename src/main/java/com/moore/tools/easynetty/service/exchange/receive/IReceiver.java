package com.moore.tools.easynetty.service.exchange.receive;

/**
 * 消息接收 接口
 * @author ：imoore
 * @date ：created in 2023/12/8 19:45
 * @version: v1.0
 */
public interface IReceiver<R> {
    /**
     * 消息接收
     *
     * @param message 消息
     * @return R
     */
    R receive(Object message);

}
