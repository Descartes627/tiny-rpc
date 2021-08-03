package com.dragonfly.tiny.rpc.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: donganguo
 * @date: 2021/8/3 10:53 上午
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest {
    /**
     * 唯一的请求id
     */
    private String requestId;
    /**
     * 接口名
     */
    private String interfaceName;
    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型集合
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数集合
     */
    private Object[] parameters;
}
