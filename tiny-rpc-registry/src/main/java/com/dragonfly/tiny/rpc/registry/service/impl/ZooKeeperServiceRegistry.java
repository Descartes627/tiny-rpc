package com.dragonfly.tiny.rpc.registry.service.impl;


import com.dragonfly.tiny.rpc.registry.service.ServiceRegistry;
import com.dragonfly.tiny.rpc.registry.constant.zkConstant;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;


import java.io.File;


/**
 * @author: donganguo
 * @date: 2021/7/29 5:41 下午
 * @Description: /registry/serviceName/serviceAddress
 */

@Slf4j
public class ZooKeeperServiceRegistry implements ServiceRegistry {

    private final ZkClient zkClient;

    public ZooKeeperServiceRegistry(String zkAddress) {
        log.debug("connecting zookeeper");
        zkClient = new ZkClient(zkAddress, zkConstant.ZK_SESSION_TIMEOUT, zkConstant.ZK_CONNECTION_TIMEOUT);
        log.debug("connected");
    }

    public void register(String serviceName, String serviceAddress) {
        // 创建 registry 节点（持久）
        if (!zkClient.exists(zkConstant.ZK_REGISTRY_PATH)) {
            zkClient.createPersistent(zkConstant.ZK_REGISTRY_PATH);
            log.debug("create registry node: {}", zkConstant.ZK_REGISTRY_PATH);
        }
        // 创建 service 节点（持久）
        String servicePath = zkConstant.ZK_REGISTRY_PATH + File.separator + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            log.debug("create service node: {}", servicePath);
        }
        // 创建 address 节点（临时）
        String addressPath = servicePath + File.separator + serviceAddress;
        if (!zkClient.exists(addressPath)) {
            String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
            log.debug("create address node: {}", addressNode);
        }
    }

    public static void main(String[] args) {
        ZooKeeperServiceRegistry zooKeeperServiceRegistry = new ZooKeeperServiceRegistry("127.0.0.1:2181");
        System.out.println(zooKeeperServiceRegistry);
    }

}
