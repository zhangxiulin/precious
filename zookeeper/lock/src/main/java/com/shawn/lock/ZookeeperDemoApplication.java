package com.shawn.lock;

public class ZookeeperDemoApplication {

    public static void main(String[] args) throws InterruptedException {
        
        DistributedLock lock = new DistributedLock();
        String lockName = lock.getLock();
        
        lock.getLock();
        
        /** 
         * 执行我们的业务逻辑
         */
        if(lockName != null) {
            lock.releaseLock(lockName);
        }
        
        lock.closeZkClient();
    }


}
