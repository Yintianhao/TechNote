
/*
 * @Author: Yintianhao
 * @Date: 2020-03-24 23:55:11
 * @LastEditTime: 2020-03-25 00:10:44
 * @LastEditors: Yintianhao
 * @Description: 使用Condition的例子
 * @FilePath: \TechNote\src\Code\UseSingleConditionWaitNotify.java
 * @Copyright@Yintianhao
 */
package Singleton;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UseSingleConditionWaitNotify {
    
    public static void main(String[] args) throws InterruptedException {
        Service service = new Service();
        ThreadA a = new ThreadA(service);
        a.start();
        Thread.sleep(3000);
        service.signal();
    }

    static class Service{
        private Lock lock = new ReentrantLock();
        public Condition condition = lock.newCondition();
        public void await(){
            lock.lock();
            try {
                System.out.println("await的时间为"+new Date().toString());
                condition.await();
                System.out.println("被唤醒");
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
        public void signal(){
            lock.lock();
            try {
                System.out.println("signal的时间为"+new Date().toString());
                condition.signal();
                Thread.sleep(3000);
                System.out.println("唤醒");
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
    }
    static public class ThreadA extends Thread{
        public Service service;
        public ThreadA(Service service){
            super();
            this.service = service;
        }
        @Override
        public void run(){
            service.await();
        }
    }
}