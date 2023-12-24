# easy-netty
easy-netty
> 简单的netty服务端客户端 纯净版
* 简单启动netty服务
  * NettyServer实例
  > NettyServer.customizableHandler()
  > 1. 追加心跳检测   
    ```
    //继承自ChannelInboundHandlerAdapter
    NettyServer.build(NettyServerHandler::new);
    //server启动端口号
    NettyServer.start(9000);
    //server关闭
    NettyServer.stop();
    ```
  * NettyClient实例
  > NettyClient.connect(ip,port)
  > 1. 连接失败时，进行重连
  > 2. 断连重新连接
  > 3. bootstrap中追加心跳检测
  ```
  
  //继承自ChannelInboundHandlerAdapter
  NettyClient.build(NettyClientHandler::new);
  //NettyClient.bind(new BaseAbstractSender());
  //连接nettyserver
  NettyClient.connect("localhost",9000);
  //发送消息
  NettyClient.send("sequence","message");
  //关闭
  NettyClient.stop();
  ```
  * NettyHelper
    ```
    //客户端向服务端发送消息时引用，每条一条有单独uuid
    //发送消息带序列
    NettyHelper.sendWithSequence(channel,"hello");
    //接收消息并获取序列（UUID）
    NettyHelper.receivedDataWithSequence(obj,(sequence,data)->{
       log.debug("sequence:{},data:{}",sequence,data);
    });
    //发送消息，不带序列（UUID）
    NettyHelper.send(channel,"hello");
    //接收没有序列（UUID）的消息
    NettyHelper.receivedData(obj,data->{
       log.debug("data:{}",data);
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
  * service.exchange.send.ISender

  > 消息发送接口
  ```
    /**
     * 消息发送
     *
     * @param channel  信道
     * @param sequence 序列，用于确认消息是否发送 可以为null，也可以为""
     * @param message 消息内容
     */
    void send(Channel channel, String sequence, String message);
  
  ```
  * service.exchange.BaseAbstractSender
  > 消息发送基础实现，可以根据构造方法有参/无参来创建实例
  ```
  //无参构建
  //在使用无参构建时，send必须使用参数存在io.netty.channel.Channel来发送消息
  new BaseAbstractSender();
  //有参构建
  new BaseAbstractSender(io.netty.channel.Channel);

    /**
     * 设置消息的预留位置
     * 在 send 方法中，这个预留位用于确保在消息的最开始和结束都有足够的空间存储序列号和消息内容的长度。
     * 在写入sequence和message时分别加了预留位，占了4个字节的空间（默认），这样使接收方能够根据预留位
     * 处存储的胀肚信息准确地解析出来序列号和消息内容
     *
     * @param reservedBit 预留位
     */
  public void setMessageReservedBit(int reservedBit)
    /**
     * 消息发送实现 
     * 抽象方法中，所有实现基于这个方法，通过重写这个方法可以实现具体功能
     * @param channel 信道
     * @param sequence 序列
     * @param message 消息
     */
    public void sendImpl(Channel channel, String sequence, String message)
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
  