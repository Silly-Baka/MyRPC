package myRPC.extension;

import com.alibaba.nacos.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date: 2022/10/10
 * Time: 19:37
 *
 * @Author SillyBaka
 * Description：SPI机制的拓展点加载器 每个标注SPI注解的类拥有自己的一个ExtensionLoader实例，并且控制该类独有的拓展点实例
 **/
@Slf4j
public class ExtensionLoader<T> {

    private static final String DIRECTORY_RESOURCES_PREFIX = "META-INF/extensions/";
    /**
     *  SPI接口与ExtensionLoader实例的映射
     */
    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    /**
     *  用于保存所有SPI接口的 实现类的单例实例对象
     */
    private static final Map<Class<?>,Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // 当前拓展点加载器实例属于哪个接口
    private Class<?> type;

    /**
     * 用于保存当前接口的实现类实例对象 并且通过实现类全类名来获取
     */
    private final Map<String,Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    /**
     * 用于保存当前接口的实现类 --- 从SPI配置文件中加载
     */
    private final Holder<Map<String,Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    /**
     * 获取拓展点加载器对象
     * @param type 声明了SPI注解的接口
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if(type == null){
            throw new IllegalArgumentException("拓展点的类型不能为空");
        }
        if(!type.isInterface()){
            throw new IllegalArgumentException("该拓展点不是接口，无法生成");
        }
        if(type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("该拓展点没有声明SPI注解，无法生成");
        }

        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if(extensionLoader == null){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }

        return extensionLoader;
    }

    /**
     * 获取当前接口某特定实现类的实例对象
     * @param name SPI配置文件中配置的名字
     */
    public T getExtension(String name){
        if(StringUtils.isBlank(name)){
            throw new IllegalArgumentException("传入的实现类全类名不能为空");
        }
        Holder<Object> holder = cachedInstances.get(name);
        if(holder == null){
            cachedInstances.putIfAbsent(name,new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if(instance == null){
            // 双重校验
            synchronized (ExtensionLoader.class){
                instance = holder.get();
                if(instance == null){
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 根据实现类的全类名创建实例对象
     * @param name SPI配置文件中配置的名字
     */
    private T createExtension(String name){
        if(StringUtils.isBlank(name)){
            throw new IllegalArgumentException("传入的全类名不能为空");
        }
        Class<?> clazz = getCachedClasses().get(name);
        if(clazz == null){
            throw new IllegalArgumentException("不存在该全类名的实现类");
        }
        // 先看看缓存中有没有
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if(instance == null){
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz,clazz.newInstance());

                instance = (T) EXTENSION_INSTANCES.get(clazz);

            } catch (InstantiationException | IllegalAccessException e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    /**
     * 获取实现类类对象的映射表
     */
    private Map<String,Class<?>> getCachedClasses(){
        // 从内存中获取
        Map<String, Class<?>> classes = cachedClasses.get();
        if(classes == null){
            synchronized (cachedClasses){
                classes = cachedClasses.get();
                if(classes == null){
                    classes = new HashMap<>();
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 从SPI目录中获取当前SPI接口的所有实现类类对象
     * @param classes 用于保存实现类类对象
     */
    private void loadDirectory(Map<String,Class<?>> classes){
        // 文件名字为SPI接口的全类名
        String fileName = DIRECTORY_RESOURCES_PREFIX + type.getName();
        try {
            Enumeration<URL> resources = ExtensionLoader.class.getClassLoader().getResources(fileName);
            if(resources != null){
                while (resources.hasMoreElements()){
                    URL url = resources.nextElement();
                    loadResource(url,classes);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadResource(URL url,Map<String,Class<?>> classes){
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            String line;
            // 读每一行
            while ((line = bufferedReader.readLine()) != null) {
                // 获取注释的下标 跳过注释
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        // 获得等号的下标 划分为记号和全类名
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // our SPI use key-value pair so both of them must not be empty
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            classes.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
