package com.moore.tools.easynetty;

import com.moore.tools.easynetty.service.NettyServer;
import com.moore.tools.easynetty.service.dm.nettychanels.NettyServerHandler;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:22
 * @description：server
 * @version: v1
 */
public class ServerTest {
    public static void main(String[] args) throws InterruptedException {
        NettyServer nettyServer = new NettyServer();
        nettyServer.addChannelHandler(NettyServerHandler::new)
                .start(9000);

//        NettyServer.build(NettyServerHandler::new);
//        NettyServer.start(9000);
//        NettyServer.stop();
    }
}
