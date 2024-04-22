package juc_senior.completablefuture;

import java.util.ArrayList;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/22 18:08
 * @description： Future 异步任务接口 结合线程池的 demo 案例
 */
public class FutureThreadPoolDemo {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        long endTime = System.currentTimeMillis();
        System.out.println("------costTime: " + (endTime - startTime) + " 毫秒");


    }

}
