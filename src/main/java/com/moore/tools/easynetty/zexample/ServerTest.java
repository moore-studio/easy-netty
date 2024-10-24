package com.moore.tools.easynetty.zexample;

import com.moore.tools.easynetty.service.NettyServer;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:22
 * @description：server
 * @version: v1
 */
public class ServerTest {
    public static void main(String[] args) throws InterruptedException {

        NettyServer nettyServer = new NettyServer();
//        nettyServer.addChannelHandler(NettyServerHandler::new)
        nettyServer.setIdentifyId("0")
                .enableHeartBeatChecking()
//                .addChannelHandler(()-> new IdleStateHandler(10,30,0, TimeUnit.SECONDS))
//                .addChannelHandler(HeartBeatsHandler::new)
                .addChannelHandler(() -> new ExampleServerHandler(nettyServer.getIdentifyId()))

                .start(9000);

//        NettyServer.build(NettyServerHandler::new);
//        NettyServer.start(9000);
//        NettyServer.stop();
    }
}
