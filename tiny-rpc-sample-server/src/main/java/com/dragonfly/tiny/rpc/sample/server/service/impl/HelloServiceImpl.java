package com.dragonfly.tiny.rpc.sample.server.service.impl;

import com.dragonfly.tiny.rpc.sample.server.dto.Person;
import com.dragonfly.tiny.rpc.sample.server.service.HelloService;
import com.dragonfly.tiny.rpc.server.annotaiton.RpcService;

/**
 * @author: donganguo
 * @date: 2021/8/3 7:09 下午
 * @Description:
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
