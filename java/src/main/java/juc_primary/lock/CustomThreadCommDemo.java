package juc_primary.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/15 22:12
 * @description： 线程定制化通信的demo，使用lock锁来实现
 *          实现案例：三个线程，分别是aa,bb,cc，分别让aa打印5次,bb打印10次,cc打印15次，按照这个顺序执行每一轮循环。
 */
class ShareResource{
    //定义标志位
    private int flag = 1; // 1 aa   2 bb  3 cc
    private Lock lock = new ReentrantLock();
    //创建3个condition,要记住从一个lock中创建
    private Condition condition1 = lock.newCondition();
    private Condition condition2 = lock.newCondition();
    private Condition condition3 = lock.newCondition();

    //打印5次方法
    void print5(int loop) throws InterruptedException {
        lock.lock();
        try{
            while(flag!=1){
                condition1.await();
            }
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName()+" :: "+i+" :: 轮数"+loop);
            }
            flag = 2;
            condition2.signal();
        }finally {
            lock.unlock();
        }
    }
    //打印10次方法
    void print10(int loop) throws InterruptedException {
        lock.lock();
        try{
            while(flag!=2){
                condition2.await();
            }
            for (int i = 1; i <= 10; i++) {
                System.out.println(Thread.currentThread().getName()+" :: "+i+" :: 轮数"+loop);
            }
            flag = 3;
            condition3.signal();
        }finally {
            lock.unlock();
        }
    }
    //打印15次
    void print15(int loop) throws InterruptedException{
        lock.lock();
        try{
            while(flag!=3){
                condition3.await();
            }
            for (int i = 1; i <= 15; i++) {
                System.out.println(Thread.currentThread().getName()+" :: "+i+" :: 轮数"+loop);
            }
            flag = 1;
            condition1.signal();
        }finally {
            lock.unlock();
        }
    }
}

public class CustomThreadCommDemo {
    public static void main(String[] args) {
        ShareResource shareResource = new ShareResource();
        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                try {
                    shareResource.print5(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"aa").start();

        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                try {
                    shareResource.print10(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"bb").start();

        new Thread(()->{
            for (int i = 1; i <=10 ; i++) {
                try {
                    shareResource.print15(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"cc").start();


    }
}
