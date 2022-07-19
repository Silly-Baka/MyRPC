# MyRPC
MyRPC是一个轻量型的手写RPC框架，网络传输基于Netty实现，注册中心使用了Nacos，并且实现了多种序列化方式和负载均衡算法（其实只有参考dubbo的RandomLoadBalance实现的权重随机算法）
# 架构

> 这幅架构图参考的是JavaGuide哥的手写rpc框架，我这里服务端还没有实现线程池

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9jbi1ndW96aXlhbmcuZ2l0aHViLmlvL015LVJQQy1GcmFtZXdvcmsvaW1nL1JQQyVFNiVBMSU4NiVFNiU5RSVCNiVFNiU4MCU5RCVFOCVCNyVBRi5qcGVn?x-oss-process=image/format,png)





# 特点

- 实现了基于Netty传输的网络传输方式，同一客户端与同一服务端的Channel连接是可复用的，避免多次连接。
- 使用Nacos作为注册中心，为服务提供者注册服务同时维护服务提供者的信息（ 由于只是简单的作为注册中心，所以替换成Zookeeper也行，并且无需过多改动）
- 实现了自定义的RPC通信协议和自定义的编解码器
- 实现了三种序列化算法：JDK原生，Json 、Kryo等 （默认使用Kryo，可以通过配置文件RpcConfig.properties修改）
- 实现了一种负载均衡算法：权重随机算法（参考Dubbo的RandomLoadBalance的算法逻辑进行实现）
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
loadbalance.type=myRPC.loadbalance.RandomLoadBalance
```

