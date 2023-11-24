# easy-netty
easy-netty
> 简单的netty服务端客户端
* 简单启动netty服务
  * server端
    ```
    //继承自ChannelInboundHandlerAdapter
    Server.build(NettyServerHandler::new);
    //server启动端口号
    Server.start(9000);
    //server关闭
    Server.stop();
    ```
  * client端
    ```
    //继承自ChannelInboundHandlerAdapter
    Client.build(NettyClientHandler::new);
    //连接nettyserver
    Client.connect("localhost",9000);
    //每向server发送一条消息都会跟一条uuid，可以做追溯
    Client.sendWithoutCallback("hello");
    //发送完消息执行回调函数
    Client.sendWithCallback("hello",()->{do something;});
    //关闭
    Client.stop();
    ```
  * NettyHelper
    ```
    //客户端向服务端发送消息时引用，每条一条有单独uuid
    //发送消息带序列
    NettyHelper.sendWithSequence(channel,"hello");
    //接收消息并获取序列（UUID）
    NettyHelper.receivedDataWithSequence(obj,(sequence,data)->{
       log.info("sequence:{},data:{}",sequence,data);
    });
    //发送消息，不带序列（UUID）
    NettyHelper.send(channel,"hello");
    //接收没有序列（UUID）的消息
    NettyHelper.receivedData(obj,data->{
       log.info("data:{}",data);
    });
    ```
  * 待完善
    * 1.server发送消息
      * 含序列
    * 2.server/client未收到消息时重新发送
    * ~~3.server/client再优化，将customizable和customizableHandler独立~~
    * 4.NettyHelper抽象
    * 5.server stop时，发送指令给客户端，客户端断开处理
    