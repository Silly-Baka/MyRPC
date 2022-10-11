package myRPC.config;

import myRPC.protocol.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Date: 2022/7/7
 * Time: 18:02
 *
 * @Author SillyBaka
 *
 * Description： 整个rpc框架的配置类
 **/
public class RpcConfig {
    static Properties properties;
    static {
        try(InputStream inputStream = RpcConfig.class.getResourceAsStream("/RpcConfig.properties")){
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new ExceptionInInitializerError();
        }
    }
    /**
     * 获得配置文件中配置的序列化算法
     * 若是没有配置 则默认返回 Jdk原生的序列化方法
     * @return 序列化算法
     */
    public static Serializer.Algorithm getSerializerAlgorithm(){
        String value = properties.getProperty("serializer.type");
        // 若是没有配置 则默认返回 Jdk原生的序列化方法
        if(value == null){
            return Serializer.Algorithm.Kryo;
        }else{
            return Serializer.Algorithm.valueOf(value);
        }
    }

    /**
     * 获取配置文件中配置的负载均衡策略
     * @return 负载均衡策略的名字
     */
    public static String getLoadBalanceType(){
        String value = properties.getProperty("loadbalance.type");
        // 若是没有配置 则默认返回权重随机算法
        if(value == null){
            return "myRPC.loadbalance.impl.RandomLoadBalance";
        }else {
            return value;
        }
    }

    /**
     * 从配置文件中获取服务提供者的实现类别名
     */
    public static String getServiceProviderName(){
        String value = properties.getProperty("serviceProvider.impl");
        if(value == null){
            return "default";
        }else {
            return value;
        }
    }

    /**
     * 从配置文件中获取服务注册表的实现类别名
     */
    public static String getServiceRegistryName(){
        String value = properties.getProperty("serviceRegistry.impl");
        if(value == null){
            return "nacos";
        }else {
            return value;
        }
    }
}
