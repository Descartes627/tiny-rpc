package com.dragonfly.tiny.rpc.sample.client;

import com.dragonfly.tiny.rpc.client.RpcProxy;

import com.dragonfly.tiny.rpc.sample.api.HelloService;
import com.dragonfly.tiny.rpc.sample.api.dto.Person;
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
    public static void main(String[] args){
        log.debug("start client");
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        Person me = Person.builder().firstName("Dong").lastName("Anguo").build();
        Person him = Person.builder().firstName("Yang").lastName("Zhian").build();
        Person him2 = Person.builder().firstName("Yang").lastName("Zhian").build();
        Person him3 = Person.builder().firstName("Yang").lastName("Zhian").build();

        String result = helloService.hello(me);
        System.out.println(result);

        String result1 = helloService.hello(him);
        System.out.println(result1);
        System.out.println(helloService.hello(him2));
        System.out.println(helloService.hello(him3));

        System.exit(0);
    }
}
