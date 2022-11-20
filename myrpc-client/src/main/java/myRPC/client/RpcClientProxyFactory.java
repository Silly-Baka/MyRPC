package myRPC.client;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import myRPC.annotation.Service;
import myRPC.handler.RpcResponseHandler;
import myRPC.protocol.RpcRequestMessage;
import myRPC.registry.ServiceRegistry;
import myRPC.registry.impl.NacosServiceRegistry;
import myRPC.utils.SequenceIdGenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * Date: 2022/7/7
 * Time: 12:22
 *
 * @Author SillyBaka
 * Description： Rpc客户端代理对象工厂
 **/
@Slf4j
public class RpcClientProxyFactory implements InvocationHandler {
    /**
     * 和Nacos进行交互的注册表
     */
    private final ServiceRegistry serviceRegistry = new NacosServiceRegistry();
    /**
     * 获取代理对象
     * @param clazz 传入的接口类
     * @return 返回代理接口对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getServiceProxy(Class<T> clazz){
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Integer sequenceId = SequenceIdGenerator.getSequenceId();
        // 把调用信息包装成 rpcRequest 然后发送给服务端
        RpcRequestMessage rpcRequestMessage = RpcRequestMessage.builder()
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .build();
        rpcRequestMessage.setSequenceId(sequenceId);
        // 从注册中心中获取服务提供者的地址
        Class<?> clazz = method.getDeclaringClass();
        InetSocketAddress address;
        // 考虑是否使用了注解进行注册，若是则判断有无别名
        if(clazz.isAnnotationPresent(Service.class)){
            String name = clazz.getAnnotation(Service.class).name();
            // 如果有别名
            if(!"".equals(name)){
                address = serviceRegistry.getServiceAddress(name,rpcRequestMessage);
            }else {
                // 否则就选择全类名
                address = serviceRegistry.getServiceAddress(clazz.getCanonicalName(),rpcRequestMessage);
            }
        // 没有使用注解注册则无别名，直接使用全类名
        }else {
            address = serviceRegistry.getServiceAddress(clazz.getCanonicalName(),rpcRequestMessage);
        }
        log.debug("服务端地址为:{}",address);
        // 获取连接对象
        Channel channel = RpcClient.getChannel(address);
        // 把请求发送给服务端 由服务端调用方法
        channel.writeAndFlush(rpcRequestMessage);
        // 用于接收结果的容器
        DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
        RpcResponseHandler.PROMISES.put(sequenceId,promise);

        // 阻塞当前进程 等待服务端返回调用的结果
        promise.await();
        if(promise.isSuccess()){
            return promise.getNow();
        }else {
            throw new RuntimeException(promise.cause());
        }
    }
}
