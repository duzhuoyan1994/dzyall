package juc_primary.jucaux;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 22:56
 * @description： juc中的辅助类，Semaphore  演示demo
 *                6辆汽车抢占3个车位
 */
public class SemaphoreDemo {
    public static void main(String[] args) {

        Semaphore sema = new Semaphore(3);

        for (int i = 1; i <=6; i++) {
            new Thread(()->{
                try {
                    //抢占信号
                    sema.acquire();
                    System.out.println(Thread.currentThread().getName()+" 抢到了车位");
                    //设置随机停车时间
                    TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                    System.out.println(Thread.currentThread().getName()+"------离开了车位");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    sema.release();
                }
            },String.valueOf(i)).start();
        }


    }


}
