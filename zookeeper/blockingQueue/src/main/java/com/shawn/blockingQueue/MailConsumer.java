package com.shawn.blockingQueue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.CreateMode;

public class MailConsumer implements Runnable, AppConstant {

    private ZkClient zkClient;
    private Lock lock;
    private Condition condition;
    

    public MailConsumer() {
        lock = new ReentrantLock();
        condition = lock.newCondition();
        zkClient = new ZkClient(new ZkConnection(ZK_CONNECT_STR));
        System.out.println("sucess connected to zookeeper server!");
        // 不存在就创建mailbox节点
        if(!zkClient.exists(NODE_PATH)) {
            zkClient.create(NODE_PATH, "this is mailbox", CreateMode.PERSISTENT);
        }
    }
    
    public void run() {
        IZkChildListener listener = new IZkChildListener() {        
          
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                System.out.println("Znode["+parentPath + "] size:" + currentChilds.size());
                // 还是要判断邮箱是否为空
                if(currentChilds.size() > 0) {
                    // 唤醒等待的线程
                    try {
                        lock.lock();
                        condition.signal();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };
        // 监视子节点的改变，不用放用while循环中，监听一次就行了，不需要重复绑定
        zkClient.subscribeChildChanges(NODE_PATH, listener);
        try {
            //循环随机发送邮件模拟真是情况
            while(true) {
                // 判断是否可以发送邮件
                checkMailReceive();
                // 接受邮件
                List<String> mailList = zkClient.getChildren(NODE_PATH);
                // 如果mailsize==0,也没有关系；可以直接循环获取就行了
                if(mailList.size() > 0) {
                    Collections.sort(mailList, new Comparator<String>() {
       
                        public int compare(String o1, String o2) {
                            return Integer.parseInt(o1.split("_")[1]) - Integer.parseInt(o2.split("_")[1]);
                        }
                    });
                    // 模拟邮件处理(0-1S)
                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
                    zkClient.delete(NODE_PATH + "/" + mailList.get(0));
                    System.out.println("mail has been received:" + NODE_PATH + "/" + mailList.get(0));
                }
            } 
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            zkClient.unsubscribeChildChanges(NODE_PATH, listener);
        }
    }

    private void checkMailReceive() {
        try {
            lock.lock();
            // 判断邮箱是为空
            List<String> mailList = zkClient.getChildren(NODE_PATH);
            System.out.println("mailbox size: " + mailList.size());
            if(mailList.size() == 0) {
                // 邮箱为空，阻塞消费者，直到邮箱有邮件
                System.out.println("mailbox is empty, please wait 。。。");
                condition.await();
                // checkMailReceive();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
