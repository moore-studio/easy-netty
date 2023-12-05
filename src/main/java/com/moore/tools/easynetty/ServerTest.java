package com.moore.tools.easynetty;

import com.moore.tools.easynetty.process.NettyServer;
import com.moore.tools.easynetty.service.security.ChannelHandler;
import com.moore.tools.easynetty.service.dm.server.ServerChannelImpl;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:22
 * @description：server
 * @version: v1
 */
public class ServerTest {
    public static void main(String[] args) throws InterruptedException {
//        NettyServer.build(NettyServerHandler::new);
        NettyServer.build(() -> new ChannelHandler(new ServerChannelImpl()));
        NettyServer.start(9000);
//        int i = 0;
//        while (true) {
//            i++;
//            Thread.sleep(1000);
//            if (i == 60) {
//                NettyServer.stop();
//                break;
//            }
//        }
    }
}
