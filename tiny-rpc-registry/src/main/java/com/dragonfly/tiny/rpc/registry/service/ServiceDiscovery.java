package com.dragonfly.tiny.rpc.registry.service;

/**
 * @author: donganguo
 * @date: 2021/7/29 5:40 下午
 * @Description:
 */
public interface ServiceDiscovery {
    /**
     * 根据服务名称查找服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    String discover(String serviceName);
}
