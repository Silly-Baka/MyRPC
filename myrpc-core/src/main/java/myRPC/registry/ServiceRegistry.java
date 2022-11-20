package myRPC.registry;

import myRPC.extension.SPI;
import myRPC.protocol.RpcRequestMessage;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;

/**
 * Date: 2022/7/12
 * Time: 15:12
 *
 * @Author SillyBaka
 * Description：远程注册表、注册中心（用于注册服务提供者）
 **/
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param aliasName 服务别名
     * @param inetSocketAddress 服务地址
     */
    void register(String serviceName, @Nullable String aliasName, InetSocketAddress inetSocketAddress);

    /**
     * 获取服务地址
     * @param serviceName 服务名称
     * @return 服务地址
     */
    InetSocketAddress getServiceAddress(String serviceName, RpcRequestMessage rpcRequest);
}
