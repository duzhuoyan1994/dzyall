package juc_primary.jucaux;

import java.util.concurrent.CyclicBarrier;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 22:42
 * @description： juc中的辅助类，CyclicBarrier  演示demo
 *                  集齐7龙珠召唤神龙
 */
public class CyclicBarrierDemo {
    private static final int num = 7;
    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(num, () -> {
            System.out.println("集齐7颗龙珠召唤神龙");
        });

        for (int i = 1; i <= 7 ; i++) {
            new Thread(()->{
                try {
                    System.out.println(Thread.currentThread().getName()+" 星龙珠被收集到了");
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }


    }


}
