package myRPC.registry.impl;

import lombok.extern.slf4j.Slf4j;
import myRPC.registry.ServiceProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Date: 2022/7/7
 * Time: 14:54
 *
 * @Author SillyBaka
 *
 * Description： 本地注册表的默认实现
 **/
@Slf4j
public class DefaultServiceProvider implements ServiceProvider {
    /**
     * 存储服务列表的map (本地服务列表）
     * 根据接口名找到相应的服务实例
     */
    private final Map<String,Object> SERVICE_MAP = new ConcurrentHashMap<>();
    /**
     * 存储已注册服务的名字列表
     */
    private final Set<String> SERVICE_NAMES = new HashSet<>();

    /**
     * 添加服务
     * 默认使用服务实例所实现的接口的完整类名 作为服务名
     * 若service实现了接口 X和Y，则注册表中X和Y会分别对应于同一个service对象
     * @param service 服务实例
     */
    @Override
    public synchronized void addService(Object service) {
        String serviceName = service.getClass().getCanonicalName();
        // 已经注册了
        if(SERVICE_NAMES.contains(serviceName)){
            return;
        }
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length == 0){
            throw new RuntimeException("该服务没有任何实现接口");
        }
        // 向该服务实例所实现的所有接口 注册服务
        for (Class<?> inter : interfaces) {
            SERVICE_MAP.put(inter.getCanonicalName(),service);
        }
        SERVICE_NAMES.add(serviceName);
        log.debug("已向接口{}注册服务{}",interfaces,serviceName);
    }

    /**
     * 通过接口名从注册表中获得服务实例
     * @param interfaceName 接口名字
     * @return 服务实例
     */
    @Override
    public Object getService(String interfaceName) {
        Object service = SERVICE_MAP.get(interfaceName);
        if(service == null){
            throw new RuntimeException("不存在该接口:"+interfaceName);
        }
        return service;
    }

    @Override
    public Set<String> getAllServiceNames() {
        return SERVICE_NAMES;
    }
}
