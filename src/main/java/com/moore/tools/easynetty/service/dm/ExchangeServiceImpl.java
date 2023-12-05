package com.moore.tools.easynetty.service.dm;

import com.moore.tools.easynetty.service.sendreceive.ExchangeAbstractService;
import io.netty.channel.Channel;

/**
 * @author ：imoore
 * @date ：created in 2023/12/5 21:12
 * @description：
 * @version: v
 */
public class ExchangeServiceImpl extends ExchangeAbstractService {
    public ExchangeServiceImpl(Channel channel) {
        super(channel);
    }

    public ExchangeServiceImpl() {
    }
}
