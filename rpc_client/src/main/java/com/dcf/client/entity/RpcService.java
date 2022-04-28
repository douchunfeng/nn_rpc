package com.dcf.client.entity;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RpcService {
    /**
     * rpc服务名
     */
    private String serviceName;

    /**
     * rpc服务地址
     */
    private String serviceAddr;

    /**
     * rpc服务端口
     */
    private int servicePort;

    /**
     * rpc服务权重
     */
    private double weight;

    /**
     * rpc服务对应channel
     */
    private Channel channel;

}
