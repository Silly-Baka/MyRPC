package myRPC.test;

import myRPC.annotation.AutoScanService;
import myRPC.registry.impl.DefaultServiceProvider;
import myRPC.registry.impl.NacosServiceRegistry;
import myRPC.server.RpcServer;
import myRPC.service.HelloService;
import myRPC.service.impl.HelloServiceImpl;

/**
 * Date: 2022/7/7
 * Time: 18:24
 *
 * @Author SillyBaka
 * Description：
 **/
@AutoScanService(basePackage = "myRPC.service")
public class testRpcServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer("localhost",8080);
        rpcServer.start();
    }
}
