package com.dcf.client.loadbalancer;

import com.dcf.client.entity.RpcService;
import com.dcf.client.util.NacosUtil;

import java.util.*;

public class ConsistentHashingLoadBalancer implements LoadBalancer {

    // 存储每一个服务对应的一致性Hash结果生成器
    public static Map<String, ConsistentHashingGenerator> generatorMap = new HashMap<>();

    @Override
    public RpcService getService(String serviceName,String sourceAddr) {
        ConsistentHashingGenerator generator;
        if ((generator=generatorMap.get(serviceName))==null){
            List<RpcService> serviceList = NacosUtil.getServiceList(serviceName);
            generator = new ConsistentHashingGenerator(serviceList);
            generatorMap.put(serviceName,generator);
        }
        return generator.getServer(sourceAddr);
    }

    static class ConsistentHashingGenerator {

        public ConsistentHashingGenerator(List<RpcService> realNodes) {
            this.realNodes = realNodes;
            this.virtualNodes = new TreeMap<>();
            this.nameServiceMap = new HashMap<>();

            for (RpcService service : realNodes) {
                System.out.println("真实节点[" + service + "] 被添加");
                String s = service.getServiceAddr() + service.getServicePort();
                nameServiceMap.put(s,service);
                // 根据权重添加虚拟结点
                for (int i = 0; i < service.getWeight(); i++) {
                    String virtualNode = s + "&&VN" + i;
                    int hash = getHash(virtualNode);
                    virtualNodes.put(hash, virtualNode);
                    System.out.println("虚拟节点[" + virtualNode + "] hash:" + hash + "，被添加");
                }
            }
        }

        /*// 虚拟节点的个数
        private int VIRTUAL_NUM = 5;*/

        // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称
        private SortedMap<Integer, String> virtualNodes;

        private HashMap<String,RpcService> nameServiceMap;

        // 真实节点列表
        private List<RpcService> realNodes ;

        /*//模拟初始服务器
        private String[] servers = {"192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.5", "192.168.1.6"};*/

        /**
         * 获取被分配的节点名
         *
         * @param node
         * @return
         */
        public RpcService getServer(String node) {
            int hash = getHash(node);
            Integer key = null;
            SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
            if (subMap.isEmpty()) {
                key = virtualNodes.firstKey();
            } else {
                key = subMap.firstKey();
            }
            String virtualNode = virtualNodes.get(key);
            return nameServiceMap.get(virtualNode.substring(0, virtualNode.indexOf("&&")));
        }

        /**
         * FNV1_32_HASH算法
         */
        private static int getHash(String str) {
            final int p = 16777619;
            int hash = (int) 2166136261L;
            for (int i = 0; i < str.length(); i++)
                hash = (hash ^ str.charAt(i)) * p;
            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;
            // 如果算出来的值为负数则取其绝对值
            if (hash < 0)
                hash = Math.abs(hash);
            return hash;
        }

        public static void main(String[] args) {
            List<RpcService> serviceList  = new ArrayList<>();
            serviceList.add(new RpcService("server1","123.125.85.156",14555,4,null));
            serviceList.add(new RpcService("server1","123.125.85.156",1459,2,null));
            serviceList.add(new RpcService("server1","123.125.85.156",8477,1,null));

            ConsistentHashingGenerator generator = new ConsistentHashingGenerator(serviceList);
            //模拟客户端的请求
            String[] nodes = {"192.168.20.137", "10.9.3.254", "192.168.10.1"};


            for (String node : nodes) {
                System.out.println("[" + node + "]的hash值为" + getHash(node) + ", 被路由到结点[" + generator.getServer(node) + "]");
            }

            for (String node : nodes) {
                System.out.println("[" + node + "]的hash值为" + getHash(node) + ", 被路由到结点[" + generator.getServer(node) + "]");
            }
        }

    }


}
