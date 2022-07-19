package myRPC.protocol;

import com.alibaba.fastjson.JSON;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Date: 2022/7/8
 * Time: 13:58
 *
 * @Author SillyBaka
 *
 * Description： 序列化器
 **/
public interface Serializer {
    /**
     * 序列化
     * @param obj 要序列化的对象
     * @return 将Java对象序列化后的字节数组
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     * @param clazz 数据类型
     * @param bytes 字节数组
     * @return 反序列化后的Java对象
     */
    <T> T deSerialize(Class<T> clazz,byte[] bytes);

    /**
     * 实现了序列化方法的枚举类
     */
    enum Algorithm implements Serializer{
        /**
         * Jdk原生的序列化方法
         */
        Java{
            @Override
            public <T> byte[] serialize(T obj) {
                ByteArrayOutputStream bos = null;
                try {
                    bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(obj);

                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化过程出现异常！序列化失败");
                }
            }
            @Override
            public <T> T deSerialize(Class<T> clazz, byte[] bytes) {
                Object object = null;
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    Object readObject = ois.readObject();

                    return (T) readObject;
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("序列化过程出现异常！序列化失败");
                }
            }
        },
        /**
         * Json序列化器 默认字符集为 UTF-8
         */
        Json{
            @Override
            public <T> byte[] serialize(T obj) {
                String jsonStr = JSON.toJSON(obj).toString();
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public <T> T deSerialize(Class<T> clazz, byte[] bytes) {
                String jsonStr = new String(bytes);
                return JSON.parseObject(jsonStr, clazz);
            }
        },
        /**
         * Kryo序列化器 默认使用
         */
        Kryo{
            private final Pool<Kryo> kryoPool = new Pool<Kryo>(true,false,16) {
                @Override
                protected Kryo create() {
                    Kryo kryo = new Kryo();
                    kryo.setReferences(true);
                    kryo.setRegistrationRequired(false);
                    return kryo;
                }
            };
            @Override
            public <T> byte[] serialize(T obj) {
                try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    Output output = new Output(bos)
                ){
                    Kryo kryo = kryoPool.obtain();
                    kryo.writeObject(output,obj);
                    output.flush();

                    kryoPool.free(kryo);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("kryo序列化出现错误");
                }
            }

            @Override
            public <T> T deSerialize(Class<T> clazz, byte[] bytes) {
                try(Input input = new Input(new ByteArrayInputStream(bytes))){
                    Kryo kryo = kryoPool.obtain();
                    input.setBuffer(bytes);

                    T object = kryo.readObject(input, clazz);

                    kryoPool.free(kryo);
                    return object;
                }
            }
        }
    }

}
