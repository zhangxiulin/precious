package com.shawn.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        // 模拟多个客户端获取配置
        executorService.submit(new ZkConfigClient());
        executorService.submit(new ZkConfigClient());
        executorService.submit(new ZkConfigClient());


    }
}
