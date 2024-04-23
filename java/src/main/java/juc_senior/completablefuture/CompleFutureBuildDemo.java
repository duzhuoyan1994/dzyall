package juc_senior.completablefuture;

import java.util.concurrent.*;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/23 12:01
 * @description： 创建CompletableFuture类的静态方法的演示demo
 */
public class CompleFutureBuildDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //有返回值的
        ExecutorService pool = Executors.newFixedThreadPool(3);
        CompletableFuture<String> comFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "hello,supplyAsync";
        }, pool);
        System.out.println(comFuture.get());
        pool.shutdown();
    }

    //没有返回值的代码示例
    private static void runAsyncDemo() throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(3);

        CompletableFuture<Void> comFuture = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        },pool);
        System.out.println(comFuture.get());
        pool.shutdown();
    }

}
