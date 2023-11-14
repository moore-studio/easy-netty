package com.moore.tools.easynetty;

import com.moore.tools.easynetty.process.Client;
import com.moore.tools.easynetty.service.nettychanels.NettyClientHandler;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:24
 * @description：client
 * @version: v
 */
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        Client.build(NettyClientHandler::new);
        Client.connect("localhost", 9000);
        for (int i = 0; i < 10; i++) {
            String message = "Hello, Netty Server ! " + i;
            Client.sendWithoutCallback(message);
            Thread.sleep(1000);
        }

        Client.stop();
    }
}
