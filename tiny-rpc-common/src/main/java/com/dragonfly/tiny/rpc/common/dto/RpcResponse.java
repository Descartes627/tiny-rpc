package com.dragonfly.tiny.rpc.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: donganguo
 * @date: 2021/8/3 10:56 上午
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
public class RpcResponse {
    /**
     * 对应的请求id
     */
    private String requestId;
    /**
     * 异常
     */
    private Exception exception;
    /**
     * 返回值
     */
    private Object result;
}
