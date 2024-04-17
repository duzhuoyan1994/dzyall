package threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/17 17:57
 * @description：  线程池的演示  demo
 */
public class ThreadPoolDemo {
    public static void main(String[] args) {
        //一池5线程
        ExecutorService fixPool = Executors.newFixedThreadPool(5);
        //一池1线程
        ExecutorService singlePool = Executors.newSingleThreadExecutor();
        //可扩容线程
        ExecutorService cachePool = Executors.newCachedThreadPool();
        try{
            for (int i = 1; i <=10 ; i++) {
                fixPool.execute(()->{
                    System.out.println(Thread.currentThread().getName()+" 办理业务");
                });
            }
        }catch (Exception e ){
            e.printStackTrace();
        }finally {
            fixPool.shutdown();
        }
    }

}
