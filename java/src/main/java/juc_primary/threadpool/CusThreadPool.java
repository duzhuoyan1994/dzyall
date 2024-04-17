package juc_primary.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/17 18:31
 * @description： 自定义线程池的案例演示  demo
 */
public class CusThreadPool {
    public static void main(String[] args) {
        //自定义线程其实就是自己去决定一些参数的值
        ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 5,
                2L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        try{
            for (int i = 1; i <=10 ; i++) {
                pool.execute(()->{
                    System.out.println(Thread.currentThread().getName()+" 办理业务");
                });
            }
        }catch (Exception e ){
            e.printStackTrace();
        }finally {
            pool.shutdown();
        }


    }

}
