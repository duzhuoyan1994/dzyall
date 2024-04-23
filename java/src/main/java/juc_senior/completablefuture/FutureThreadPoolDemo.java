package juc_senior.completablefuture;

import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/22 18:08
 * @description： Future 异步任务接口 结合线程池的 demo 案例
 */
public class FutureThreadPoolDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //3个任务，目前开启多个异步线程任务来处理，耗时多少
        long startTime = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        FutureTask<String> task1 = new FutureTask<>(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "task1 over";
        });
        //异步提交第1个任务
        pool.submit(task1);

        FutureTask<String> task2 = new FutureTask<>(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "task2 over";
        });
        //异步提交第2个任务
        pool.submit(task2);
        //获得返回值，加上get之后时间变长
        System.out.println(task1.get());
        System.out.println(task2.get());

        //main线程处理第三个
        try {
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("------costTime: " + (endTime - startTime) + " 毫秒");
        System.out.println(Thread.currentThread().getName() + " ------end");
        //开启线程池要记得关闭资源
        pool.shutdown();

    }

    private static void m1() {
        //3个任务，目前只有一个main方法来处理，3个try表示3个任务，耗时多少
        long startTime = System.currentTimeMillis();
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("------costTime: " + (endTime - startTime) + " 毫秒");
        System.out.println(Thread.currentThread().getName() + " ------end");
    }

}
