package myRPC.client;

import myRPC.service.HelloService;

/**
 * Date: 2022/7/7
 * Time: 18:24
 *
 * @Author SillyBaka
 * Description：
 **/
public class testRpcClient {
    public static void main(String[] args) {
        RpcClientProxyFactory clientProxyFactory = new RpcClientProxyFactory();
        HelloService helloService = clientProxyFactory.getServiceProxy(HelloService.class);
        System.out.println(helloService.hello("张三"));
    }
}
