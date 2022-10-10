package myRPC.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: 2022/7/18
 * Time: 14:57
 *
 * @Author SillyBaka
 *
 * Description：用于服务端的启动类，用于标注扫描服务的包
 *              默认是当前启动类所处的包
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoScanService {
    // 包名
    String[] basePackage() default {""};
}
