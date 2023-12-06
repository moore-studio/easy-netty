package com.moore.tools.easynetty;

import com.moore.tools.easynetty.process.NettyClient;
import com.moore.tools.easynetty.service.NettyHelper;
import com.moore.tools.easynetty.service.dm.nettychanels.NettyClientHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:24
 * @description：client
 * @version: v
 */
@Slf4j
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        NettyClient.build(NettyClientHandler::new);
        NettyClient.connect("localhost", 9000);
        for (int i = 0; i < 10; i++) {
            NettyHelper.send(NettyClient.channelFuture.channel(),"", ("Hello, Netty Server ! " + i));
            Thread.sleep(1000);
        }

        NettyClient.stop();
    }

}
