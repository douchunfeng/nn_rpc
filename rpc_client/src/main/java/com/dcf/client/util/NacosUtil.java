package com.dcf.client.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.dcf.client.entity.RpcService;
import com.dcf.common.config.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NacosUtil {
    /**
     * nacos服务
     */
    private static NamingService namingService;

    private static ConcurrentHashMap<String, List<RpcService>> servicesRecord = new ConcurrentHashMap<>();

    static {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", Config.getNacoServerAddr());

            namingService = NamingFactory.createNamingService(properties);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    public static List<RpcService> getServiceList(String serviceName) {
        List<RpcService> rpcServices = servicesRecord.get(serviceName);
        if(rpcServices==null){
            try {
                List<Instance> instances = namingService.getAllInstances(serviceName);
                namingService.subscribe(serviceName, new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        // TODO: 更新服务列表
                    }
                });
                List<RpcService> services= new CopyOnWriteArrayList<>();
                for (Instance instance:instances){
                    services.add(new RpcService(instance.getServiceName(),
                            instance.getIp(),instance.getPort(),instance.getWeight(),null));
                }
                servicesRecord.put(serviceName,services);
                return servicesRecord.get(serviceName);
            } catch (NacosException e) {
                e.printStackTrace();
                System.out.println("无法获取服务名对应rpc服务");
            }
        }
        return rpcServices;
    }

    public static void main(String[] args) throws NacosException {
        System.out.println(getServiceList("rpc_server1"));
    }
}
