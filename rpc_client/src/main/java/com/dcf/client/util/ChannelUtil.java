package com.dcf.client.util;

import com.dcf.client.entity.RpcService;
import com.dcf.client.handler.RpcResponseMessageHandler;
import com.dcf.client.loadbalancer.ConsistentHashingLoadBalancer;
import com.dcf.client.loadbalancer.LoadBalancer;
import com.dcf.client.loadbalancer.WeightRandomLoadBalancer;
import com.dcf.common.protocol.RpcMessageCodec;
import com.dcf.common.protocol.DatagramInitialDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class ChannelUtil {
    private static ConsistentHashingLoadBalancer clb = new ConsistentHashingLoadBalancer();
    private static WeightRandomLoadBalancer wlb = new WeightRandomLoadBalancer();


    // 获取唯一的 channel 对象
    public static Channel getChannel(String serviceName) throws UnknownHostException {
        // 可根据配置使用不同的LoadBalancer对象
        LoadBalancer loadBalancer = wlb;

        RpcService service = loadBalancer.getService(serviceName, InetAddress.getLocalHost().getHostAddress());

        if (service.getChannel() != null) {
            return service.getChannel();
        }
        // 避免并发操作异常
        synchronized (service) {
            if (service.getChannel() != null) {
                return service.getChannel();
            }
            Channel channel = initChannel(service);
            service.setChannel(channel);
            return channel;
        }
    }

    // 初始化 channel 方法
    private static Channel initChannel(RpcService service) {
        Channel channel = null;
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        RpcMessageCodec MESSAGE_CODEC = new RpcMessageCodec();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new DatagramInitialDecoder());
                // ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
            }
        });
        try {
            channel = bootstrap.connect(service.getServiceAddr(), service.getServicePort()).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
        return channel;
    }
}
