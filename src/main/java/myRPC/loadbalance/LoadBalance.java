package myRPC.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * Date: 2022/7/16
 * Time: 21:23
 *
 * @Author SillyBaka
 *
 * Description：负载均衡器底层接口
 **/
public interface LoadBalance {
    /**
     * 采用负载均衡算法从服务列表中选择一个服务实例
     * @param instances 服务列表
     * @return 选出的服务实例
     */
    Instance selectServiceInstance(List<Instance> instances);
}
