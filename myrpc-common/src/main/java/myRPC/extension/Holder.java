package myRPC.extension;

/**
 * Date: 2022/10/10
 * Time: 19:42
 *
 * @Author SillyBaka
 * Description：用于维护实例对象线程安全的holder -- 生存周期为runtime
 **/
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }
    public void set(T value){
        this.value = value;
    }
}
