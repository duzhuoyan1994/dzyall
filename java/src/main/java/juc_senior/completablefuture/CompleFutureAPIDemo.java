package juc_senior.completablefuture;

import java.util.concurrent.*;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/24 11:43
 * @description： CompletableFuture常见方法的代码案例demo
 *  获得结果和触发计算
 *  对计算结果进行处理
 *  对计算结果进行消费
 *  对计算速度进行选用
 *  对计算结果进行合并
 */
public class CompleFutureAPIDemo {
    public static void main(String[] args) throws Exception {
        demo1();
        demo2();
        demo3();
        demo4();
        demo5();
        demo6();
    }
    // 对计算结果进行合并
    private static void demo6() {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "----come in");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "----come in");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 2;
        });
        CompletableFuture<Integer> result = future1.thenCombine(future2, Integer::sum);
        System.out.println(result.join());
    }

    //对计算速度进行选用
    private static void demo5() {
        CompletableFuture<String> playa = CompletableFuture.supplyAsync(() -> {
            //模拟1号选手，需要2s
            System.out.println("A come in");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "playA";
        });
        CompletableFuture<String> playb = CompletableFuture.supplyAsync(() -> {
            //模拟12号选手，需要3s
            System.out.println("B come in");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "playB";
        });
        CompletableFuture<String> res = playa.applyToEither(playb, f -> {
            return f + " is winner";
        });
        System.out.println(Thread.currentThread().getName()+"-----"+res.join());
    }

    //对计算结果进行消费
    private static void demo4() {
        CompletableFuture.supplyAsync(()->{
            return 1;
        }).thenApply(f->{
            return f+2;
        }).thenApply(f->{
            return f+3;
        }).thenAccept(System.out::println);
    }

    //对计算结果进行处理   使用handle方法
    private static void demo3() {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("111");
            return 1;
        },pool).handle((f,e)->{
            int i = 10/0;
            System.out.println("222");
            return f+2;
        }).handle((f,e)->{
            System.out.println("333");
            return f+3;
        }).whenComplete((v,e)->{
            if(e==null){
                System.out.println("------计算结果：" + v);
            }
        }).exceptionally( e ->{
            e.printStackTrace();
            System.out.println(e.getCause()+"---->"+e.getMessage());
            return null;
        });
        System.out.println(Thread.currentThread().getName() + "-----主线程先去忙其他任务");

        pool.shutdown();
    }


    //对计算结果进行处理   使用thenApply方法
    private static void demo2() {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("111");
            return 1;
        },pool).thenApply(f->{
            System.out.println("222");
            return f+2;
        }).thenApply(f->{
            System.out.println("333");
            return f+3;
        }).whenComplete((v,e)->{
            if(e==null){
                System.out.println("------计算结果：" + v);
            }
        }).exceptionally( e ->{
            e.printStackTrace();
            System.out.println(e.getCause()+"---->"+e.getMessage());
            return null;
        });
        System.out.println(Thread.currentThread().getName() + "-----主线程先去忙其他任务");

        pool.shutdown();
    }

    //获得结果和触发计算demo案例
    private static void demo1() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "hello,abc";
        });
//        System.out.println(future.get());
//        System.out.println(future.get(2, TimeUnit.SECONDS));
//        System.out.println(future.join());
//        System.out.println(future.getNow("xxx")); //如果没有计算完成，返回给定的默认值
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(future.complete("complele") + "\t" + future.join());
    }

}
