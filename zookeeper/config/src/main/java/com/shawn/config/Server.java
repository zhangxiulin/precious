package com.shawn.config;

import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) throws Exception {
        ZkConfigMng zkConfigMng = new ZkConfigMng();
        zkConfigMng.initConfig(null);
        zkConfigMng.syncConfigToZookeeper();
        TimeUnit.SECONDS.sleep(10);
        
        // 修改值
        zkConfigMng.update(new CommonConfig("jdbc:mysql://192.168.1.122:3306/mydata?useUnicode=true&characterEncoding=utf-8",
                "root", "wxh", "com.mysql.jdbc.Driver"));

    }
}
