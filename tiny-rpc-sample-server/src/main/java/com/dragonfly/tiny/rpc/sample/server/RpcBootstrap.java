package com.dragonfly.tiny.rpc.sample.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: donganguo
 * @date: 2021/8/3 7:05 下午
 * @Description:
 */
@Slf4j
public class RpcBootstrap {
    public static void main(String[] args) {
        log.info("start server");
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
