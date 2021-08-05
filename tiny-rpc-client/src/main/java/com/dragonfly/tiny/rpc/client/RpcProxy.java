package com.dragonfly.tiny.rpc.client;

import com.dragonfly.tiny.rpc.common.dto.RpcRequest;
import com.dragonfly.tiny.rpc.common.dto.RpcResponse;
import com.dragonfly.tiny.rpc.registry.service.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author: donganguo
 * @date: 2021/8/3 5:47 下午
 * @Description: 动态代理Rpc接口，invoke中发送网络请求
 */
@Slf4j
public class RpcProxy {

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    String serviceAddress = "";
                    //创建RPC请求对象
                    RpcRequest request = RpcRequest.builder()
                            .requestId(UUID.randomUUID().toString())
                            .interfaceName(method.getDeclaringClass().getName())
                            .serviceVersion(serviceVersion)
                            .methodName(method.getName())
                            .parameterTypes(method.getParameterTypes())
                            .parameters(args)
                            .build();

                    // 获取 RPC 服务地址
                    if (serviceDiscovery != null) {
                        String serviceName = interfaceClass.getName();
                        if (!StringUtils.isEmpty(serviceVersion)) {
                            serviceName += "-" + serviceVersion;
                        }
                        serviceAddress = serviceDiscovery.discover(serviceName);
                        log.debug("discover service: {} => {}", serviceName, serviceAddress);
                    }
                    if (StringUtils.isEmpty(serviceAddress)) {
                        throw new RuntimeException("server address is empty");
                    }

                    // 从 RPC 服务地址中解析主机名与端口号
                    String[] array = serviceAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);

                    // 创建 RPC 客户端对象并发送 RPC 请求
                    RpcClient rpcClient = new RpcClient(host, port);
                    long time = System.currentTimeMillis();
                    rpcClient.send(request);
                    RpcResponse response = rpcClient.getResponse();
                    log.debug("{} Spend {}ms", request.getRequestId(), System.currentTimeMillis() - time);
                    if (response == null) {
                        throw new RuntimeException("response is null");
                    }
                    // 返回 RPC 响应结果
                    if (response.getException() != null) {
                        throw response.getException();
                    } else {
                        return response.getResult();
                    }
                }
        );
    }
}
