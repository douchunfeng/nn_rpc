package com.dcf.client.loadbalancer;

import com.dcf.client.entity.RpcService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface LoadBalancer {

    RpcService getService(String serviceName,String sourceAddr);


}
