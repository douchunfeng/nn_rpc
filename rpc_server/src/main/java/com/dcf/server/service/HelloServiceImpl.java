package com.dcf.server.service;

import com.dcf.api.HelloService;
import com.dcf.common.config.Config;
import org.springframework.stereotype.Component;

@Component
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String msg) {
        // int i = 1 / 0;
        return "你好, " + msg + Config.getServicePort();
    }
}