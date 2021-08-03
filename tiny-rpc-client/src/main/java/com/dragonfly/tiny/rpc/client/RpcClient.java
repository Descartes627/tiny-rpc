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

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        this.response = response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("api caught exception", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RpcDecoder(RpcResponse.class));    //in
                            pipeline.addLast(new RpcEncoder(RpcRequest.class));     //out
                            pipeline.addLast(this);                                 //in
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true);
            // 绑定套接字
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 写入 RPC 请求数据并关闭连接
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync().addListener((ChannelFutureListener) channelFuture -> log.debug("Send request for response" + request.getRequestId()));
            channel.closeFuture().sync();
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }
}
