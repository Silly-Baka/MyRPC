package myRPC.registry;

import myRPC.extension.SPI;

import java.util.Set;

/**
 * @author SillyBaka
 *
 *  本地注册表，用于服务提供者注册服务
 */
@SPI
public interface ServiceProvider {
    /**
     * 添加服务
     * @param service 服务实例
     */
    void addService(Object service);

    /**
     * 根据接口名获取服务实例
     * @param interfaceName 接口名字
     * @return 服务实例
     */
    Object getService(String interfaceName);

    /**
     * 获取当前服务端已注册的所有服务名
     * @return 服务名集合
     */
    Set<String> getAllServiceNames();
}
