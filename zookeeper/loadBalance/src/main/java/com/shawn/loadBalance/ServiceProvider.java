package com.shawn.loadBalance;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.CreateMode;

public class ServiceProvider {

 // 静态常量
    static String ZK_CONNECT_STR = "127.0.0.1:2181";
    static String NODE_PATH = "/service";
    static String SERIVCE_NAME = "/myService";
    
    private ZkClient zkClient;
    
    public ServiceProvider() {
        zkClient = new ZkClient(new ZkConnection(ZK_CONNECT_STR));
        System.out.println("sucess connected to zookeeper server!");
        // 不存在就创建NODE_PATH节点
        if(!zkClient.exists(NODE_PATH)) {
            zkClient.create(NODE_PATH, "this is mailbox", CreateMode.PERSISTENT);
        }
    }
    
    public void registryService(String localIp, Object obj) {
        if(!zkClient.exists(NODE_PATH + SERIVCE_NAME)) {
            zkClient.create(NODE_PATH + SERIVCE_NAME, "provider services list", CreateMode.PERSISTENT);
        }
        // 对自己的服务进行注册
        zkClient.createEphemeral(NODE_PATH + SERIVCE_NAME + "/" + localIp, obj);
        System.out.println("注册成功！[" + localIp + "]");
    }   

}
