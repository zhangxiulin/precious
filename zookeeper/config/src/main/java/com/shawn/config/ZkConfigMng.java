package com.shawn.config;

import org.I0Itec.zkclient.ZkClient;

public class ZkConfigMng {
    private String nodePath = "/commConfig";
    private CommonConfig commonConfig;
    private ZkClient zkClient;
    
    public CommonConfig initConfig(CommonConfig commonConfig) {
        if(commonConfig == null) {
            this.commonConfig = new CommonConfig("jdbc:mysql://127.0.0.1:3306/mydata?useUnicode=true&characterEncoding=utf-8",
                    "root", "root", "com.mysql.jdbc.Driver");   
        } else {
            this.commonConfig = commonConfig;
        }
        return this.commonConfig;
    }
    
    /**
     * 更新配置
     * 
     * @param commonConfig
     * @return
     */
    public CommonConfig update(CommonConfig commonConfig) {
        if(commonConfig != null) {
            this.commonConfig = commonConfig;
        }
        syncConfigToZookeeper();
        return this.commonConfig;
    }
    
    public void syncConfigToZookeeper() {
        if(zkClient == null) {
            zkClient = new ZkClient("127.0.0.1:2181");
        }
        if(!zkClient.exists(nodePath)) {
            zkClient.createPersistent(nodePath);
        }
        zkClient.writeData(nodePath, commonConfig);
    }

}
