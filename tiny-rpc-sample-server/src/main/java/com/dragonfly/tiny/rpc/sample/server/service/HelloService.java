package com.dragonfly.tiny.rpc.sample.server.service;

import com.dragonfly.tiny.rpc.sample.server.dto.Person;

/**
 * @author: donganguo
 * @date: 2021/8/3 7:05 下午
 * @Description:
 */
public interface HelloService {
    String hello(String name);

    String hello(Person person);
}
