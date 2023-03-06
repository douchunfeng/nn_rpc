package com.dcf.server;


import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.dcf.common.config.Config;
import com.dcf.common.protocol.RpcMessageCodec;
import com.dcf.common.protocol.DatagramInitialDecoder;
import com.dcf.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@Slf4j
public class RpcServer {
    public static void main(String[] args)  {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        RpcMessageCodec MESSAGE_CODEC = new RpcMessageCodec();
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new DatagramInitialDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(Config.getServicePort()).sync().channel();
            boss.execute(()->{
                register();
            });
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private static void register() {
        // register to nacos server
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", Config.getNacoServerAddr());

            NamingService namingService = NamingFactory.createNamingService(properties);

            namingService.registerInstance(Config.getServiceName(), InetAddress.getLocalHost().getHostAddress(),Config.getServicePort());
            //namingService.registerInstance(Config.getServiceName(), InetAddress.getLocalHost().getHostAddress(),9000);
            //namingService.registerInstance("rpc_service2", "123.32.43.132",Config.getServicePort());

            //List<Instance> allInstances = namingService.getAllInstances(Config.getServiceName());
        } catch (NacosException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
