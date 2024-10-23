package com.moore.tools.easynetty.zexample;

import com.moore.tools.easynetty.service.NettyClient;
import com.moore.tools.easynetty.service.dm.nettychanels.NettyClientHandler;
import com.moore.tools.easynetty.service.dm.nettychanels.SenderImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:24
 * @description：client
 * @version: v
 */
@Slf4j
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        NettyClient nettyClient = new NettyClient();
//        nettyClient.addChannelHandler(NettyClientHandler::new)
        nettyClient.addChannelHandler(() -> new ExampleClientHandler(nettyClient.getIdentifyId()))
                .setIdentityId("1")
                .bind(new SenderImpl())
                .connect("localhost", 9000);

//        for (int i = 0; i < 10; i++) {
//            nettyClient.send(UUID.randomUUID().toString(), ("Hello, Netty Server ! " + i));
//
//        }
        Thread.sleep(120000);
        nettyClient.stop();
        //静态方法
        /*
        NettyClient.build(NettyClientHandler::new);
        NettyClient.bind(new BaseAbstractSender() {
        });
        NettyClient.connect("localhost", 9000);
        for (int i = 0; i < 10; i++) {
            NettyClient.send( ("Hello, Netty Server ! " + i));
            Thread.sleep(1000);
        }

        NettyClient.stop();*/
    }


}
