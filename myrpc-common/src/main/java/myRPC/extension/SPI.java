package myRPC.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: 2022/10/10
 * Time: 19:38
 *
 * @Author SillyBaka
 * Description：SPI注解 只有声明了该注解的类可以使用拓展点机制
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SPI {
}
