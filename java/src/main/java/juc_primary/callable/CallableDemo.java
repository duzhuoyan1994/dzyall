package juc_primary.callable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 18:29
 * @description： juc_primary.callable 接口的demo演示
 */
class MyThread1 implements Runnable{
    @Override
    public void run() {
    }
}
class MyThread2 implements Callable{
    @Override
    public Integer call() throws Exception {
        return 200;
    }
}
public class CallableDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //Runnable
        new Thread(new MyThread1(),"aa").start();

        //Callable
        FutureTask<Integer> task1 = new FutureTask<>(new MyThread2());

        FutureTask<Integer> task2 =new FutureTask<>(()-> {
            System.out.println(Thread.currentThread().getName()+" come in juc_primary.callable");
            return 1024;
        });

        //创建一个线程
        new Thread(task2,"lucy").start();
        while(!task2.isDone()){
            System.out.println("wait...");
        }
        System.out.println(task2.get()); //只有第一次的get方法需要计算，之后如果再调用get的话，直接返回
        System.out.println(Thread.currentThread().getName()+" come over");

    }


}
