package com.shawn.loadBalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.CreateMode;

public class ServiceConsumer {

 // 静态常量
    static String ZK_CONNECT_STR = "127.0.0.1:2181";
    static String NODE_PATH = "/service";
    static String SERIVCE_NAME = "/myService";
    
    private List<String> serviceList = new ArrayList<String>();
    
    private ZkClient zkClient;
    
    public ServiceConsumer() {
        zkClient = new ZkClient(new ZkConnection(ZK_CONNECT_STR));
        System.out.println("sucess connected to zookeeper server!");
        // 不存在就创建NODE_PATH节点
        if(!zkClient.exists(NODE_PATH)) {
            zkClient.create(NODE_PATH, "this is mailbox", CreateMode.PERSISTENT);
        }
    }
    
    /**
     * 订阅服务
     */
    public void subscribeSerivce() {
        serviceList = zkClient.getChildren(NODE_PATH + SERIVCE_NAME);
        zkClient.subscribeChildChanges(NODE_PATH + SERIVCE_NAME, new IZkChildListener() {
       
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                serviceList = currentChilds;
            }
        });
    }
    
    /**
     * 模拟调用服务
     */
    public void consume() {
        //负载均衡算法获取某台机器调用服务
        int index = new Random().nextInt(serviceList.size());
        System.out.println("调用[" + NODE_PATH + SERIVCE_NAME + "]服务：" + serviceList.get(index));
    }


}
