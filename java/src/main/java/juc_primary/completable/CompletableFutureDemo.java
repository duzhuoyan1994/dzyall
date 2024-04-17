package juc_primary.completable;

import java.util.concurrent.CompletableFuture;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/17 22:06
 * @description： CompletableFuture 异步调用的 有返回值和没有返回值的demo
 */
public class CompletableFutureDemo {
    public static void main(String[] args) throws Exception {
        //异步调用，没有返回值的
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(()->{
            System.out.println(Thread.currentThread().getName()+" future1");
        });
        future1.get();

        //异步调用，有返回值的
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " future2");
            int i = 1/0; //手动异常
            return 1024;
        });
        future2.whenComplete((t,u)->{
            System.out.println("---t="+t); //返回的值
            System.out.println("---u="+u); //如果有异常的话，异常信息在这个u里面
        }).get();

    }
}
