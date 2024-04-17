package juc_primary.jucaux;

import java.util.concurrent.CountDownLatch;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 22:32
 * @description：  juc中的辅助类，CountDownLatch 这个类的功能 演示示例 demo
 *                  6个同学陆续离开教室，班长才可以锁门
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        //创建对象，设置初始值
        CountDownLatch latch = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+" 号同学离开了教室");
                //有人离开之后，初始值-1
                latch.countDown();
            },String.valueOf(i)).start();
        }
        //等待操作
        latch.await();
        System.out.println(Thread.currentThread().getName()+" 班长锁门走人了");
    }
}
