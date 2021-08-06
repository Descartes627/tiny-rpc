package com.dragonfly.tiny.rpc.client;

import com.dragonfly.tiny.rpc.common.codec.RpcDecoder;
import com.dragonfly.tiny.rpc.common.codec.RpcEncoder;
import com.dragonfly.tiny.rpc.common.dto.RpcRequest;
import com.dragonfly.tiny.rpc.common.dto.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: donganguo
 * @date: 2021/8/3 5:22 下午
 * @Description: 发送请求 接收响应
 */
@Slf4j
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private final String host;
    private final int port;

    private RpcResponse response;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse getResponse() {
        return response;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        log.debug("RpcClient读到response " + response);
        this.response = response;
        ctx.close();
//        response.notifyAll();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("api caught exception", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Channel channel = null;
        try {
            // 创建并初始化 Netty 客户端 Bootstrap 对象
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new RpcEncoder(RpcRequest.class)); // 编码 RPC 请求 out
                    pipeline.addLast(new RpcDecoder(RpcResponse.class)); // 解码 RPC 响应 in
                    pipeline.addLast(RpcClient.this); // 处理 RPC 响应  in
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            // 连接 RPC 服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 写入 RPC 请求数据并关闭连接
            channel = future.channel();
            channel.writeAndFlush(request).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.debug("write request");
                }
            });
            channel.closeFuture().sync();
            // 返回 RPC 响应对象
            return response;
        } finally {
            group.shutdownGracefully();
            channel.close();
        }
    }
}
