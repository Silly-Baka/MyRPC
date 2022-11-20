package myRPC.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import myRPC.protocol.RpcRequestMessage;

import java.util.List;

/**
 * Date: 2022/7/16
 * Time: 21:26
 *
 * @Author SillyBaka
 *
 * Description：负载均衡器的抽象类，用于实现负载均衡算法
 **/
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public Instance selectServiceInstance(List<Instance> serviceProviders,RpcRequestMessage rpcRequest) {
        if(serviceProviders == null || serviceProviders.size() == 0){
            return null;
        }
        if(serviceProviders.size() == 1){
            return serviceProviders.get(0);
        }
        return doSelect(serviceProviders,rpcRequest);
    }

    /**
     * 采用负载均衡算法从服务列表中选择一个服务实例 （包装实际算法逻辑）
     * @param rpcRequest 服务列表
     * @return 选出的服务实例
     */
    protected abstract Instance doSelect(List<Instance> serviceProviders, RpcRequestMessage rpcRequest);
}
