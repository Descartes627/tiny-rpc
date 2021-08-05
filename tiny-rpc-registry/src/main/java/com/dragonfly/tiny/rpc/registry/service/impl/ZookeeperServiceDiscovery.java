package com.dragonfly.tiny.rpc.registry.service.impl;

import com.dragonfly.tiny.rpc.registry.service.ServiceDiscovery;
import com.dragonfly.tiny.rpc.registry.constant.zkConstant;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.jboss.netty.util.internal.ThreadLocalRandom;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;

/**
 * @author: donganguo
 * @date: 2021/8/2 5:43 下午
 * @Description: /registry/serviceName/serviceAddress data:serviceAddress
 */
@Slf4j
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private String zkAddress;

    public ZookeeperServiceDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public String discover(String serviceName) {
        ZkClient zkClient = new ZkClient(zkAddress, zkConstant.ZK_SESSION_TIMEOUT, zkConstant.ZK_CONNECTION_TIMEOUT);
        log.info("connect zookeeper");
        try {
            String servicePath = zkConstant.ZK_REGISTRY_PATH + File.separator + serviceName;
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
            }
            //获取子节点的名字，非全名
            List<String> addressList = zkClient.getChildren(servicePath);
            if (CollectionUtils.isEmpty(addressList)) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }
            String address;
            int size = addressList.size();
            if (size == 1) {
                address = addressList.get(0);
            }
            else {
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
                log.info("get random address node: {}", address);
            }
            String addressPath = servicePath + File.separator + address;
            return zkClient.readData(addressPath);
        }
        finally {
            zkClient.close();
        }
    }
}
