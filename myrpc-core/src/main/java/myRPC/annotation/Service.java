package myRPC.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: 2022/7/18
 * Time: 14:55
 *
 * @Author SillyBaka
 *
 * Description：用于服务端标注服务
 *              默认是当前类名
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    // 服务名字
    String name() default "";
}
