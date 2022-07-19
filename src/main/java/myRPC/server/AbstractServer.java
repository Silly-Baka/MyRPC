package myRPC.server;

import myRPC.annotation.AutoScanService;
import myRPC.annotation.Service;
import myRPC.registry.ServiceProvider;
import myRPC.registry.ServiceRegistry;
import myRPC.utils.ReflectUtils;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * Date: 2022/7/18
 * Time: 17:01
 *
 * @Author SillyBaka
 * Description：服务端抽象类，实现了自动注册方法，启动方法由其子类实现
 **/
public abstract class AbstractServer {
    protected ServiceProvider serviceProvider;
    protected ServiceRegistry serviceRegistry;
    /**
     * 启动服务器 由子类实现
     */
    public abstract void start();
    /**
     * 自动扫描并注册服务
     * @param host 服务端的域名
     * @param port 端口号
     */
    public void scanServices(String host,int port){
        String startClassName = ReflectUtils.getStartClassName();
        try {
            Class<?> startClass = Thread.currentThread().getContextClassLoader().loadClass(startClassName);
            // 查看启动类是否有使用@AutoScanService注解
            if(!startClass.isAnnotationPresent(AutoScanService.class)){
                // 若没有使用则直接返回
                return;
            }
            // 获得要扫描的包
            String[] basePackage = startClass.getAnnotation(AutoScanService.class).basePackage();
            // 获得包内的所有类
            Set<Class<?>> classes = ReflectUtils.getClassesByPackageName(basePackage);

            InetSocketAddress address = new InetSocketAddress(host, port);
            // 遍历判断类是否带有@Service注解
            for (Class<?> clazz : classes) {
                // 有则注册服务
                if(clazz.isAnnotationPresent(Service.class)){
                    String serviceName = clazz.getAnnotation(Service.class).name();
                    if("".equals(serviceName)){
                        // 默认注册该类实现的所有接口作为服务名 别名为空
                        addService(clazz,null,address);
                    }else {
                        // 使用别名作为服务名
                        addService(clazz,serviceName,address);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * 向本地和nacos注册服务
     * @param clazz 服务实例的类
     * @param aliasName 服务别名 可以为空 若为空则默认使用实现类的全类名
     * @param address 服务端地址
     */
    public void addService(Class<?> clazz,@Nullable String aliasName, InetSocketAddress address){
        Object service = null;
        try {
            service = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        serviceProvider.addService(service);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            serviceRegistry.register(anInterface.getCanonicalName(),aliasName,address);
        }
    }
}
