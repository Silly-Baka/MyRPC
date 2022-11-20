# 架构

> 这幅架构图参考的是JavaGuide哥的手写rpc框架

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9jbi1ndW96aXlhbmcuZ2l0aHViLmlvL015LVJQQy1GcmFtZXdvcmsvaW1nL1JQQyVFNiVBMSU4NiVFNiU5RSVCNiVFNiU4MCU5RCVFOCVCNyVBRi5qcGVn?x-oss-process=image/format,png)





# 特点

- 实现了基于Netty传输的网络传输方式，同一客户端与同一服务端的Channel连接是可复用的，避免多次连接。
- 使用Nacos作为注册中心，为服务提供者注册服务同时维护服务提供者的信息（ 由于只是简单的作为注册中心，所以替换成Zookeeper也行，并且无需过多改动）
- 实现了自定义的RPC通信协议和自定义的编解码器
- 实现了三种序列化算法：JDK原生，Json 、Kryo等 （默认使用Kryo，可以通过配置文件RpcConfig.properties修改）
- 实现了两种负载均衡算法：权重随机算法（参考Dubbo的RandomLoadBalance的算法逻辑进行实现）、一致性哈希算法（参考Dubbo的ConsistenHashLoadBanlance）
- 实现了利用注解进行自动注册服务的功能，可以使用@Service注解来标注服务，然后在启动类中使用@AutoScanService进行自动扫描并注册
- 接口抽象良好，模块耦合度低，序列化器、负载均衡算法等方式可使用配置文件进行配置



# 框架模块概览

annotation ———— 框架所实现的注解

client  ———— 客户端以及客户端代理工厂的实现

config ———— 配置类

handler ———— 用于Netty处理消息的处理器，包含了Rpc请求和响应的处理器

loadbalance ———— 负载均衡器的实现

protocol ———— 本RPC框架所使用的自定义协议，包括消息实体类、序列化器、自定义编解码器等

registry ———— 用于服务注册的类，包含了本地注册表和与注册中心进行交互的远程注册表

server ———— 服务端的实现

service ———— 存放服务接口

utils ———— 工具类的实现



# 自定义传输协议

```java
*  |  Magic Number | Control Num | Order Type | Serializer Type| SequenceId | None Sense | Data Length |
*  |    4 bytes    |   1 byte    |   1 byte   |    1 bytes     |  4 bytes   |  1 byte    |   4 bytes   |
*  +---------------------------------------------------+---------------+-----------------+-------------+
*  |                                           Data Bytes                                              |
*  |                                      Length: ${Data Length}                                       |
*   +--------------------------------------------------------------------------------------------------+
```

| 字段            | 注释                                                       |
| --------------- | :--------------------------------------------------------- |
| Magic Number    | 魔数，用于标识协议包，默认使用的是baka                     |
| Control Num     | 版本控制号，用于控制协议的版本                             |
| Order Type      | 消息的类型，比如说这是rpc请求信息，那么类型就是rpc请求类型 |
| Serializer Type | 序列化方法，标明该消息使用了哪种序列化方法                 |
| SequenceId      | 消息的唯一Id                                               |
| None Sense      | 无意义的字节，用于占位                                     |
| Data Length     | 数据的实际字节长度                                         |



# 快速开始

## 1）编写服务端的服务接口（实现类要加上@Service注解）

```java
public interface HelloService {
    String hello(String name);
}
```

```java
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "你好"+name+"远程调用成功！！";
    }
}
```



## 2）编写服务端（提供者）

​		**启动服务端前要确保已经连接了Nacos，并且连接于本地的 8848 端口！！！**

```java
@AutoScanService(basePackage = "myRPC.service")   // 自动扫描注解，填写要扫描的包名
public class testRpcServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer("localhost",8080); // 参数为服务端的地址
        rpcServer.start();
    }
}
```

## 3）编写客户端进行远程调用（消费者）

```java
public class testRpcClient {
    public static void main(String[] args) {
        // 1、获得一个客户端代理工厂对象
        RpcClientProxyFactory clientProxyFactory = new RpcClientProxyFactory(); 
        // 2、通过代理工厂获得要远程调用的接口的代理对象
        HelloService helloService = clientProxyFactory.getServiceProxy(HelloService.class);
        // 3、远程调用
        System.out.println(helloService.hello("张三"));
    }
}
```

## 注意事项

1、**启动服务端前要确保已经连接了Nacos，并且连接于本地的 8848 端口！！！**

2、**要先启动服务端再启动客户端**



# 配置文件

请务必命名为 **RpcConfig.properties** ！！，并且存放于resources文件夹下

目前的 key 有：

serilizer.type  ———— 用于配置序列化方法（Java、Json、Kryo 三种值）

oadbalance.type ———— 用于配置负载均衡算法 （只有权重随机算法：myRPC.loadbalance.RandomLoadBalance ，日后会增加映射，减少配置长度）

例子：

```properties
serializer.type=Kryo
loadbalance.type=random

# SPI机制配置实现类
serviceProvider.impl=default
serviceRegistry.impl=nacos
```





# 有待改正和增加的地方

- [ ] @Service注解填写服务名存在bug，服务端注册服务若是使用别名进行注册，**那么客户端要如何知道这个别名（原因在于注册中心只能通过服务名来获取服务信息）**然后通过别名从注册中心中获取服务地址等信息。**现在的问题就是：客户端要如何知道？**已有的解决方案：①通过数据库来获得（效率太低，并且数据量少，不至于使用） ②使用Netty进行网络通信获取，但要跟哪个服务端进行通信呢？现在连服务端地址都不知道  ③使用中间件，例如消息队列、Redis等进行缓存（效率较高）④有一种设想：能否使用注册中心来存放别名映射等信息呢
- [x] Netty服务端增加线程池（专门执行服务方法），提高异步执行请求的效率，**提高服务端处理请求的吞吐量**

> **`Netty虽然使用了线程池，但从Netty的工作流程可知，每一个pipeline绑定一个EventLoop，而一个EventLoop可以绑定多个Channel，即一个线程需要监听多个客户端的消息 同时还需要处理这些消息在pipeline中的处理。那么一但有某个handler处理的时间过长，该线程所绑定的客户端的所有的请求和消息在这期间都会被阻塞，导致处理请求的吞吐量大大降低`**
>
>  **这时就可以考虑将处理时间较长的handler交给一个异步线程池来处理，处理完之后再根据上下文交给下一个handler（但这里要如何实现线程同步呢，）**
>
> ![](C:/Users/86176/Desktop/%E7%AC%94%E8%AE%B0/MyRPC%E7%9A%84ReadMe.assets/1708060-20211110232551788-397426171.png)



- [x] 增加Netty心跳机制，避免服务端与客户端之间的连接断掉
- [ ] 压缩请求信息的字节，可以使用 javaassist或者gzip ？
- [x] 使用**SPI机制**
- [ ] 集成Spring
- [x] 使用MQ来解决**请求积压**的问题？ ---->  请求积压：当远程调用请求发送的很频繁  而服务端来不及处理 这时候要考虑服务端的架构  **`（ 请求积压不能用MQ来解决 只能水平拓展服务端集群，使用多线程来提高服务端的处理速度，从而提高请求的吞吐量）`**

