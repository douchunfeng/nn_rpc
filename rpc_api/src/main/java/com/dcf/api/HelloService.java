package com.dcf.api;

import com.dcf.common.annotation.RpcService;


@RpcService(serviceName = "rpc_service1")
public interface HelloService {
    String sayHello(String name);
}
