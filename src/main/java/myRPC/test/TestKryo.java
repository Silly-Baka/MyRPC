package myRPC.test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import myRPC.service.HelloService;
import myRPC.service.impl.HelloServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Date: 2022/7/10
 * Time: 11:07
 *
 * @Author SillyBaka
 * Description：
 **/
public class TestKryo {
    public static void main(String[] args) {
        Kryo kryo = new Kryo();
        kryo.register(Person.class);

        Person person = new Person("niubi", 12);

        // 序列化
        Pool<Output> outputPool = new Pool<Output>(true,false,10) {
            @Override
            protected Output create() {
                return new Output(1024,-1);
            }
        };
        Output output = outputPool.obtain();
        kryo.writeObject(output,person);
        byte[] bytes = output.getBuffer();
        output.reset();
        outputPool.free(output);

        // 反序列化
        Input input = new Input(new ByteArrayInputStream(bytes));
        Person object = kryo.readObject(input, Person.class);

        System.out.println(object);
    }
}
