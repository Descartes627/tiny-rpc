package com.dragonfly.tiny.rpc.server;

import com.dragonfly.tiny.rpc.common.codec.RpcDecoder;
import com.dragonfly.tiny.rpc.common.codec.RpcEncoder;
import com.dragonfly.tiny.rpc.common.dto.RpcRequest;
import com.dragonfly.tiny.rpc.common.dto.RpcResponse;
import com.dragonfly.tiny.rpc.registry.service.ServiceRegistry;
import com.dragonfly.tiny.rpc.server.annotaiton.RpcService;
import com.dragonfly.tiny.rpc.server.handler.RpcServiceHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * @author: donganguo
 * @date: 2021/8/3 3:13 下午
 * @Description: 接收请求，处理请求，发送响应，实现ApplicationContextAware接口可以拿到Bean容器
 */
@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private String serviceAddress;

    private ServiceRegistry serviceRegistry;

    public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * key:serviceName-serviceVersion, value:serviceBean
     */
    private final Map<String, Object> handlerMap = new HashMap<>();

    /**
     * Bean容器会注入到ctx, 获取@RpcService的Bean，将其存入handlerMap
     * @param ctx
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (!CollectionUtils.isEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService annotation = serviceBean.getClass().getAnnotation(RpcService.class);
                String serviceName = annotation.value().getName();
                String serviceVersion = annotation.version();
                if (!StringUtils.isEmpty(serviceVersion)) {
                    serviceName += "-" + serviceVersion;
                }
                handlerMap.put(serviceName, serviceBean);
            }
        }
    }

    /**
     * 引导netty服务器，并在zookeeper上注册服务
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();  //acceptor
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();    //服务所有socket读写和不耗时或立即返回的任务
        final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(10);    //处理耗时任务
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RpcDecoder(RpcRequest.class));
                            pipeline.addLast(new RpcEncoder(RpcResponse.class));
                            pipeline.addLast(executorGroup, new RpcServiceHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 获取 RPC 服务器的 IP 地址与端口号
            String[] addressArray = serviceAddress.split(":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            // 监听端口
            ChannelFuture future = bootstrap.bind(ip, port).sync();
            // zookeeper上注册 RPC 服务地址
            if (serviceRegistry != null) {
                for (String interfaceName : handlerMap.keySet()) {
                    serviceRegistry.register(interfaceName, serviceAddress);
                    log.debug("register service: {} => {}", interfaceName, serviceAddress);
                }
            }
            log.debug("server started on port {}", port);
            // 关闭 RPC 服务器
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
