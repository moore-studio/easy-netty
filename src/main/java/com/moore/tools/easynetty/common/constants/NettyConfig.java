package com.moore.tools.easynetty.common.constants;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 * @date ：created in 2023/12/5 21:38
 * @description：netty配置实体
 * @version: v
 */
@Slf4j
@Data
public class NettyConfig {
    /**
     * 重连间隔时间（秒）
     */
    private Integer reconnectDelayTime;

    /**
     * 最大重连次数
     */
    private Integer reconnectMaxRetries;
    /**
     * 最大连接次数
     */
    private Integer connectedMaxRetries;
}
