package com.dcf.client;

import com.alibaba.nacos.api.exception.NacosException;
import com.dcf.api.HelloService;
import com.dcf.client.handler.RpcResponseMessageHandler;
import com.dcf.client.util.ChannelUtil;
import com.dcf.common.annotation.RpcService;
import com.dcf.common.message.RpcRequestMessage;
import com.dcf.common.protocol.SequenceIdGenerator;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

@Slf4j
public class RpcClient {

    public static void main(String[] args) throws NacosException {
        //Instance instance = namingService.selectOneHealthyInstance("rpc_server1");
        HelloService service = getProxyService(HelloService.class);
        System.out.println(service.sayHello("zhangsan"));
        System.out.println(service.sayHello("lisi"));
        System.out.println(service.sayHello("wangwu"));
        System.out.println(service.sayHello("zhangsan"));
        System.out.println(service.sayHello("lisi"));
        System.out.println(service.sayHello("wangwu"));
        System.out.println(service.sayHello("zhangsan"));
        System.out.println(service.sayHello("lisi"));
        System.out.println(service.sayHello("wangwu"));
    }

    // 创建代理类
    public static <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};

        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转换为 消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );
            // 2. 将消息对象发送出去
            // 获取rpc服务名
            String serviceName = null;
            if(interfaces[0].isAnnotationPresent(RpcService.class)){
                RpcService annotation = interfaces[0].getAnnotation(RpcService.class);
                serviceName = annotation.serviceName();
            }else{
                throw new RuntimeException("所调用的接口不是rpc服务");
            }
            Channel channel = ChannelUtil.getChannel(serviceName);
            channel.writeAndFlush(msg);

            // 3. 准备一个空 Promise 对象，来接收结果
            DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            // 4. 等待 promise 结果
            promise.await();
            if (promise.isSuccess()) {
                // 调用正常
                return promise.getNow();
            } else {
                // 调用失败
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }


}
