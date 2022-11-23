package myRPC;

import myRPC.client.RpcClientProxyFactory;
import myRPC.service.RetryService;

/**
 * Description：
 * <p>Date: 2022/11/23
 * <p>Time: 14:40
 *
 * @Author SillyBaka
 **/
public class testRetryClient {
    public static void main(String[] args) {
        RpcClientProxyFactory clientProxyFactory = new RpcClientProxyFactory();
        RetryService serviceProxy = clientProxyFactory.getServiceProxy(RetryService.class);

        serviceProxy.retry();
    }
}
