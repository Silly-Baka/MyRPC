package myRPC;

import myRPC.annotation.AutoScanService;
import myRPC.registry.impl.DefaultServiceProvider;
import myRPC.registry.impl.NacosServiceRegistry;
import myRPC.server.RpcServer;
import myRPC.service.HelloService;
import myRPC.service.impl.HelloServiceImpl;

import java.net.InetSocketAddress;

/**
 * Date: 2022/7/16
 * Time: 22:14
 *
 * @Author SillyBaka
 * Description：
 **/
@AutoScanService(basePackage = "myRPC.service")
public class testRpcServer2 {
    public static void main(String[] args) {
        DefaultServiceProvider defaultServiceRegistry = new DefaultServiceProvider();
        HelloService helloService = new HelloServiceImpl();
        NacosServiceRegistry nacosServiceRegistry = new NacosServiceRegistry();

        RpcServer rpcServer = new RpcServer("localhost",8081);
        rpcServer.start();
    }
}
