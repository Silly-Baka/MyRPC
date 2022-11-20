package myRPC;
import myRPC.annotation.AutoScanService;
import myRPC.server.RpcServer;

/**
 * Date: 2022/7/7
 * Time: 18:24
 *
 * @Author SillyBaka
 * Description：
 **/
@AutoScanService(basePackage = "myRPC.service")
public class testRpcServer1 {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer("localhost",8080);
        rpcServer.start();
    }
}
