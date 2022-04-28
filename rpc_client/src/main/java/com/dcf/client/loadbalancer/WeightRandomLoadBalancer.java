package com.dcf.client.loadbalancer;

import com.dcf.client.entity.RpcService;
import com.dcf.client.util.NacosUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WeightRandomLoadBalancer implements LoadBalancer{

    @Override
    public RpcService getService(String serviceName,String sourceAddr) {
        List<RpcService> services = NacosUtil.getServiceList(serviceName);
        int weightSum = 0;
        for (RpcService rpcService:services){
            weightSum += rpcService.getWeight();
        }
        int pos = new Random().nextInt(weightSum);

        int start=0;
        for (RpcService rpcService:services){
            if(start<=pos&&pos<start+rpcService.getWeight()){
                return rpcService;
            }
            start+=rpcService.getWeight();
        }
        return null;
    }

    public static void main(String[] args) {
        List<RpcService> services  = new CopyOnWriteArrayList<>();
        services.add(new RpcService("22","2121",21,1,null));
        services.add(new RpcService("22","2121",21,3,null));
        services.add(new RpcService("22","2121",21,10,null));
        for (int i = 0; i < 10; i++) {
            int weightSum = 0;
            for (RpcService rpcService:services){
                weightSum += rpcService.getWeight();
            }
            int pos = new Random().nextInt(weightSum);

            int start=0;
            for (RpcService rpcService:services){
                if(start<=pos&&pos<start+rpcService.getWeight()){
                    System.out.println(rpcService);
                    break;
                }
                start+=rpcService.getWeight();
            }
        }

    }



}
