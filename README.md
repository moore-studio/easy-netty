# easy-netty
easy-netty
> 简单的netty服务端客户端
> 1. service.dm -> demo做测试时使用最终会统一优化
> 2. entities最终会统一优化（删除？）
> 3. 客户端与服务端刚建立连接时:
>    1. 客户端向服务端发送RSA公钥（测试完毕后要改为只生成一次）
>    2. 服务端接收到客户端发送的公钥后，向客户端发送服务端公钥（所有客户端统一一个公钥）
>       1. 客户端 -> RSA密钥 -> （publicKey）发送 -> 服务端
>       2. 服务端接收到来自客户端的公钥 -> RSA密钥 -> （publicKey）发送 -> 客户端
>       3. 客户端用服务端的公钥进行数据加密 -> 服务端 私钥解密
>       4. 服务端用客户端的公钥加密 -> 客户端私钥解密
* 简单启动netty服务
  * server端
  > NettyServer.customizableHandler()
  > 1. 追加心跳检测   
    ```
    //继承自ChannelInboundHandlerAdapter
    Server.build(NettyServerHandler::new);
    //server启动端口号
    Server.start(9000);
    //server关闭
    Server.stop();
    ```
  * client端
  > NettyClient.connect(ip,port)
  > 1. 连接失败时，进行重连
  > 2. 断连重新连接
  > 3. bootstrap中追加心跳检测
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
  * service.condition.IOperation
  > if else优化，每个业务模块实现来自IOperation.execute,通过存到Map中进行判断
  ```
  Map<String,IOperation> map = new HashMap(){{
  put("a",OperationImpl1());
  put("b",OperationImpl2());
  put("c",OperationImpl3());
  }};
  IOperation Operation = Optional.ofNullable(map.get("a")).orElse("c");
  Operation.execute();
  ```
  * service.sendreceive.IExchangeService
  > 数据收发接口
  ```
    /**
     * 消息发送
     * @param channel 信道
     * @param message 数据
     */
  
  IExchangeService.sender(Channel channel,String message);
  
    /**
     * 消息发送
     * @param message 数据
     */
    void sender(String message);

    /**
     * 消息发送
     * @param type 类型
     * @param message 消息
     */
    <P extends NettyEntity> void sender(CommandSendType type, P message);

    /**
     * 消息接收
     * @param channel 信道
     * @param message 数据
     * @return entity
     */
    String received(Channel channel,Object message);
  
    /**
     * 消息接收
     * @param message 数据
     * @return entity
     */
    String received(Object message);
  
  ```
    
  * 待完善
    * 1.server发送消息
      * 含序列
    * 2.server/client未收到消息时重新发送
    * ~~3.server/client再优化，将customizable和customizableHandler独立~~
    * 4.NettyHelper抽象
    * 5.server stop时，发送指令给客户端，客户端断开处理
    * Server中追加SSL
    * 自定义协议
    