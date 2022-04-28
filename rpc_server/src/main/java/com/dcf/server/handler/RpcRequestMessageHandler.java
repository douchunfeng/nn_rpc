package com.dcf.server.handler;

import com.dcf.api.HelloService;
import com.dcf.common.message.RpcRequestMessage;
import com.dcf.common.message.RpcResponseMessage;
import com.dcf.server.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    private static ApplicationContext ac;

    static {
        ac = new FileSystemXmlApplicationContext("classpath:/spring.xml");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(message.getSequenceId());
        try {
            // HelloService service = (HelloService)ServicesFactory.getService(Class.forName(message.getInterfaceName()));
            // 使用spring注解获取bean
            Object bean = ac.getBean(Class.forName(message.getInterfaceName()));
            Method method = bean.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
            Object invoke = method.invoke(bean, message.getParameterValue());
            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getCause().getMessage();
            response.setExceptionValue(new Exception("远程调用出错:" + msg));
        }
        ctx.writeAndFlush(response);
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RpcRequestMessage message = new RpcRequestMessage(
                1,
                "com.dcf.api.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        );
        Object bean = ac.getBean(Class.forName(message.getInterfaceName()));
        Method method = bean.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        Object invoke = method.invoke(bean, message.getParameterValue());
        System.out.println(invoke);
    }

}