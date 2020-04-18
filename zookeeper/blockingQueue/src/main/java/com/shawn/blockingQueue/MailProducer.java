package com.shawn.blockingQueue;

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

public class MailProducer implements Runnable, AppConstant {

    private ZkClient zkClient;
    private Lock lock;
    private Condition condition;

    /**
     * 初始化状态
     */
    public MailProducer() {
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
                // 还是要判断邮箱是否已满
                if(currentChilds.size() < MAILBOX_SIZE) {
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
                checkMailSend();
                // 发送邮件
                String cretePath = zkClient.createEphemeralSequential(NODE_PATH + CHILD_NODE_PATH, "your mail");
                System.out.println("your mail has been send:" + cretePath);
                // 模拟随机间隔的发送邮件(0-10S)
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000));
            } 
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            zkClient.unsubscribeChildChanges(NODE_PATH, listener);
        }

    }

    private void checkMailSend() {
        try {
            lock.lock();
            // 判断邮箱是否已满
            List<String> mailList = zkClient.getChildren(NODE_PATH);
            System.out.println("mailbox size: " + mailList.size());
            if(mailList.size() >= MAILBOX_SIZE) {
                // 邮箱已满，阻塞生产者，直到邮箱有空间
                System.out.println("mailbox is full, please wait 。。。");
                condition.await();
                checkMailSend();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
