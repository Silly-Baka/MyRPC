package myRPC.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Date: 2022/7/16
 * Time: 21:30
 *
 * @Author SillyBaka
 *
 * Description：参考dubbo的RandomLoadBalance所实现的权重随机算法
 **/
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected Instance doSelect(List<Instance> instances) {
        double totalWeight = 0;
        int length = instances.size();
        // 判断全部权重是否相同
        boolean isSameWeight = true;
        for (int i = 0; i < length; i++) {
            double weight = instances.get(i).getWeight();
            totalWeight += weight;
            // 权重不同
            if(isSameWeight && i > 0 && weight != instances.get(i-1).getWeight()){
                isSameWeight = false;
            }
        }
        if(totalWeight > 0 && !isSameWeight){
            // 获取一个范围在0-totalWeight的随机权重
            double randomWeight = ThreadLocalRandom.current().nextDouble(totalWeight);
            // 获取随机值所在的切片
            for (Instance instance : instances) {
                totalWeight -= instance.getWeight();
                // 说明就在当前切片
                if (totalWeight < 0) {
                    return instance;
                }
            }
        }
        // 如果权重全都相同或者权重全为0 则均等随机
        return instances.get(ThreadLocalRandom.current().nextInt(length));
    }
}
