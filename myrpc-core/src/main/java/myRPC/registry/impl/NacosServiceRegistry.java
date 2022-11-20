package myRPC.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import myRPC.config.RpcConfig;
import myRPC.extension.ExtensionLoader;
import myRPC.loadbalance.LoadBalance;
import myRPC.protocol.RpcRequestMessage;
import myRPC.registry.ServiceProvider;
import myRPC.registry.ServiceRegistry;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 2022/7/12
 * Time: 15:12
 *
 * @Author SillyBaka
 *
 * Description：使用Nacos作为注册中心
 **/
public class NacosServiceRegistry implements ServiceRegistry {
    /**
     * 与Nacos进行交互的容器
     */
    private static final NamingService NAMING_SERVICE;
    /**
     * 负载均衡器
     */
    private static final LoadBalance LOAD_BALANCER;
//    /**
//     * 用于服务的别名映射
//     */
//    private static final Map<String,String> SERVICE_MAP;
    static {
        try {
            NAMING_SERVICE = NamingFactory.createNamingService(RpcConfig.getNacosServerList());
            // 从配置文件中获取负载均衡策略的短词
            String loadBalanceType = RpcConfig.getLoadBalanceType();
//            // 通过反射创造负载均衡器
//            LOAD_BALANCER = (LoadBalance) Class.forName(loadBalanceType).getConstructor().newInstance();

            //todo 根据SPI机制获取指定实现类
            LOAD_BALANCER = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalanceType);

////             保证线程安全
//            SERVICE_MAP = new ConcurrentHashMap<>();
        } catch (NacosException e) {
            throw new RuntimeException("nacos注册中心初始化时出现异常:"+e);
        }
    }
    @Override
    public void register(String serviceName,String aliasName,InetSocketAddress inetSocketAddress) {
        try {
            // 别名为空 则直接使用serviceName注册
            if(aliasName == null){
                NAMING_SERVICE.registerInstance(serviceName,inetSocketAddress.getHostName(),inetSocketAddress.getPort());
            }else {
                // 否则使用别名进行注册 并建立serviceName与别名之间的映射关系
                NAMING_SERVICE.registerInstance(aliasName,inetSocketAddress.getHostName(),inetSocketAddress.getPort());
//                SERVICE_MAP.put(serviceName,aliasName);
            }
        } catch (NacosException e) {
            throw new RuntimeException("nacos注册服务时出现异常");
        }
    }
    @Override
    public InetSocketAddress getServiceAddress(String serviceName, RpcRequestMessage rpcRequest) {
        try {
//            // 如果有别名
//            if(SERVICE_MAP.containsKey(serviceName)){
//                serviceName = SERVICE_MAP.get(serviceName);
//            }
            List<Instance> serviceProviders = NAMING_SERVICE.getAllInstances(serviceName);
            // 负载均衡 选择一个合适的服务提供者
            Instance instance = LOAD_BALANCER.selectServiceInstance(serviceProviders,rpcRequest);
            if(instance == null){
                throw new RuntimeException("nacos中不存在该服务实例");
            }
            // 返回服务提供者的地址
            return new InetSocketAddress(instance.getIp(),instance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException("nacos获取服务时出现异常");
        }
    }

    /**
     * 当服务器关闭时调用，注销当前服务器所提供的所有服务
     * @param serviceProvider 当前服务器的本地服务注册表
     * @param address 当前服务器的地址
     */
    public static void clearAllService(ServiceProvider serviceProvider,InetSocketAddress address){
        Set<String> serviceNames = serviceProvider.getAllServiceNames();
        String host = address.getHostName();
        int port = address.getPort();
        for (String serviceName : serviceNames) {
            try {
                NAMING_SERVICE.deregisterInstance(serviceName, host, port);
            } catch (NacosException e) {
                throw new RuntimeException("nacos反注册服务失败,当前服务端为:" + address);
            }
        }
    }
}
