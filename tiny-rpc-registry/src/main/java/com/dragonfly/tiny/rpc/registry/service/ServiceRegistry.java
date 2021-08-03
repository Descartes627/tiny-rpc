package com.dragonfly.tiny.rpc.registry.service;

/**
 * @author: donganguo
 * @date: 2021/7/29 5:39 下午
 * @Description:
 */
public interface ServiceRegistry {
    /**
     * 注册服务名称与服务地址
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);
}
