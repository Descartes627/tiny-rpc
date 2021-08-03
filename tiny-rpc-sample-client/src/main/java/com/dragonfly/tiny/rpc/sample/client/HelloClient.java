package com.dragonfly.tiny.rpc.sample.client;

import com.dragonfly.tiny.rpc.client.RpcProxy;

import com.dragonfly.tiny.rpc.sample.api.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: donganguo
 * @date: 2021/8/3 8:22 下午
 * @Description:
 */
@Slf4j
public class HelloClient {
    public static void main(String[] args) {
        log.debug("start client");
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        System.out.println(rpcProxy);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("world");
//        System.out.println(result);

        System.exit(0);
    }
}
