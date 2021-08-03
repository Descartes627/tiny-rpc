package com.dragonfly.tiny.rpc.server.annotaiton;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: donganguo
 * @date: 2021/8/3 2:14 下午
 * @Description: @Component注册bean
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {
    /**
     *服务接口
     */
    Class<?> value();

    /**
     * 服务版本号
     */
    String version() default "";
}
