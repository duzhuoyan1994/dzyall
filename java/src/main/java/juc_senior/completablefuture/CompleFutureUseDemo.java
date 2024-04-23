package juc_senior.completablefuture;

import java.util.concurrent.*;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/23 12:16
 * @description： CompletableFuture的基本用法
 */
public class CompleFutureUseDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //使用CompletableFuture 的加强版用法
        ExecutorService pool = Executors.newFixedThreadPool(3);

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "---come in");
            int result = ThreadLocalRandom.current().nextInt(10);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("----1s以后获取结果" + result);
            return result;
        },pool).whenComplete((v,e) -> {
            if(e == null){
                System.out.println("------异步任务计算完成，没有异常，更新系统值："+v);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            System.out.println("异常情况：" + e.getCause() + ":::" + e.getMessage());
            return null;
        });
        System.out.println(Thread.currentThread().getName()+"---先去忙了");
        pool.shutdown();

        /**
         * 让主线程不要立刻结束，因为主线程一结束，CompletableFuture使用的线程池会立刻关闭(因为用的是默认的线程池，如果是自定义线程池不用有这种情况)，
         * 等待不到异步线程执行完成就直接关闭了， 看不到执行完之后调用whenComplete方法中的代码
         */
//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    //未优化版本的异步线程调用，使用CompletableFuture进行实现，也是可以实现的
    private static void demo1() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "---come in");
            int result = ThreadLocalRandom.current().nextInt(10);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("----1s以后获取结果" + result);
            return result;
        });
        System.out.println(Thread.currentThread().getName()+"---先去忙了");
        System.out.println(future.get());
    }

}
