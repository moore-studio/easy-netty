package com.moore.tools.easynetty;

import com.moore.commonutil.security.rsa.RSA;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.enums.CommandSendType;
import com.moore.tools.easynetty.process.NettyClient;
import com.moore.tools.easynetty.service.dm.ExchangeServiceImpl;
import com.moore.tools.easynetty.service.dm.client.ClientChannelImpl;
import com.moore.tools.easynetty.service.security.ChannelHandler;
import com.moore.tools.easynetty.service.SendHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author ：imoore
 * @date ：created in 2023/11/15 0:24
 * @description：client
 * @version: v
 */
@Slf4j
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
//        NettyClient.build(NettyClientHaimplndler::new);
        NettyClient.bindExchange(ExchangeServiceImpl.class);
        NettyClient.build(() -> new ChannelHandler(new ClientChannelImpl()));
        NettyClient.connect("localhost", 9000);
        int p = 0;
        while (Objects.isNull(ClientChannelImpl.channelEntity.getPublicKey())) {
            log.info("等待与服务器交换密钥" + p);
            Thread.sleep(1000);
            p++;
        }
        for (int i = 0; i < 10; i++) {
            NettyEntity entity = new NettyEntity();
            entity.setFormAddress("127.0.0.1");
            entity.setClientId(ClientChannelImpl.CLIENT_ID);
            entity.setData(RSA.encrypt(ClientChannelImpl.channelEntity.getPublicKey(), "Hello, Netty Server ! " + i));
            NettyClient.exchange().sender(CommandSendType.PUT, entity);
//            SendHelper.Client.send(NettyClient.channelFuture.channel(), CommandSendType.PUT, entity);
//            String message = ;
//            NettyClient.sendWithoutCallback(message);
            Thread.sleep(1000);
        }


        NettyClient.stop();
    }

}
