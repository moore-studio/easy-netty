package com.moore.tools.easynetty;

import com.moore.tools.easynetty.process.Server;
import com.moore.tools.easynetty.service.nettychanels.NettyServerHandler;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:22
 * @description：server
 * @version: v1
 */
public class ServerTest {
    public static void main(String[] args) throws InterruptedException {
        Server.build(NettyServerHandler::new);
        Server.start(9000);
        int i = 0;
        while (true) {
            i++;
            Thread.sleep(1000);
            if (i == 60) {
                Server.stop();
                break;
            }
        }
    }
}
