package myRPC.loadbalance.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import myRPC.annotation.Service;
import myRPC.loadbalance.AbstractLoadBalance;
import myRPC.protocol.RpcRequestMessage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description：基于一致性哈希算法的负载均衡器
 * <p>Date: 2022/11/15
 * <p>Time: 10:18
 *
 * @Author SillyBaka
 **/
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    // 用于保存每个服务的虚拟节点
    private final ConcurrentHashMap<String,ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected Instance doSelect(List<Instance> serviceProviders, RpcRequestMessage rpcRequest) {

        // 用于确认服务提供者是否已扩容
        int identityHashCode = System.identityHashCode(serviceProviders);

        String serviceName = rpcRequest.getInterfaceName();

        ConsistentHashSelector consistentHashSelector = selectors.get(serviceName);

        // 检测是否为空或扩容
        if(consistentHashSelector == null || consistentHashSelector.identifyHashCode != identityHashCode){
            selectors.put(serviceName,new ConsistentHashSelector(serviceProviders,160,identityHashCode));
            consistentHashSelector = selectors.get(serviceName);
        }

        // 对请求方法名、参数类表进行哈希 再找节点
        return consistentHashSelector.select(serviceName + Arrays.stream(rpcRequest.getParameters()));
    }

    // 一致性哈希选择器，用于初始化虚拟节点以及选择节点
    static class ConsistentHashSelector{

        // 每个服务的哈希环
        private final TreeMap<Long,Instance> virtualInstances;

        // 用于检测是否扩容的hash
        private final int identifyHashCode;

        public ConsistentHashSelector(List<Instance> seviceProviders, int replicaNumber, int identifyHashCode){
            this.virtualInstances = new TreeMap<>();
            this.identifyHashCode = identifyHashCode;

            // 初始化虚拟节点
            for (Instance seviceProvider : seviceProviders) {
                String serviceAddress = seviceProvider.getIp()+seviceProvider.getPort();
                for (int i = 0; i < replicaNumber/4; i++) {
                    byte[] digest = md5(serviceAddress+i);
                    for (int j = 0; j < 4; j++) {
                        long hash = hash(digest,j);
                        virtualInstances.put(hash,seviceProvider);
                    }
                }
            }
        }

        // 对请求方法名、参数类表进行哈希 再找节点
        public Instance select(String serviceKey){
            byte[] digest = md5(serviceKey);

            return selectForKey(hash(digest,0));
        }

        public Instance selectForKey(long hashCode){
            Map.Entry<Long, Instance> entry = virtualInstances.tailMap(hashCode, true).firstEntry();

            if(entry == null){
                entry = virtualInstances.firstEntry();
            }

            return entry.getValue();
        }

        // 将字符串转化为MD5字节数组
        static byte[] md5(String serviceAddress){
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("md5");
                byte[] bytes = serviceAddress.getBytes(StandardCharsets.UTF_8);
                md5.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e.getMessage(),e);
            }
            return md5.digest();
        }

        static long hash(byte[] digest,int num){
            return  ((long) digest[3 + num * 4] & 0xff << 24 |
                    (long) digest[2 + num * 4] & 0xff << 16 |
                    (long) digest[1 + num * 4] & 0xff << 8 |
                    (long) digest[num * 4] & 0xff)
                    & 0xfffffffL;
        }
    }
}
