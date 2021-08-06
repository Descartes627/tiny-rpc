package com.dragonfly.tiny.rpc.server.handler;

import com.dragonfly.tiny.rpc.common.dto.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: donganguo
 * @date: 2021/8/3 2:16 下午
 * @Description: RPC 服务端处理器（用于处理 RPC 请求）
 */
@Slf4j
public class RpcServiceHandler extends SimpleChannelInboundHandler<RpcRequest> {

    /**
     * key:serviceName-serviceVersion, value:serviceBean
     */
    private final Map<String, Object> handlerMap;

    public RpcServiceHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 采用多线程异步处理请求
     * @param ctx
     * @param request
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
//        ctx.executor().submit(() -> {
//            RpcResponse response = new RpcResponse();
//            response.setRequestId(request.getRequestId());
//            try {
//                Object result = handle(request);
//                response.setResult(result);
//            }
//            catch (Exception e) {
//                log.error("handle result failure", e);
//                response.setException(e);
//            }
//            ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> log.info("Send response for request " + request.getRequestId()));
//        });
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
        }
        catch (Exception e) {
            log.error("handle result failure", e);
            response.setException(e);
        }
        ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> log.info("create response for request " + request.getRequestId()));
    }

    private Object handle(RpcRequest request) throws Exception {
        String serviceName = request.getInterfaceName();
        String serviceVersion = request.getServiceVersion();
        if (!StringUtils.isEmpty(serviceVersion)) {
            serviceName += "-" + serviceVersion;
        }

        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        // 执行反射调用
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);
        //cglib反射
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server caught exception", cause);
        ctx.close();
    }
}
