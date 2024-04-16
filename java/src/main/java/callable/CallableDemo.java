package callable;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 18:29
 * @description： callable 接口的demo演示
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
    public static void main(String[] args) {
        //Runnable
        new Thread(new MyThread1(),"aa").start();

        //Callable
        FutureTask<Integer> task1 = new FutureTask<>(new MyThread2());

        FutureTask<Integer> task2 =new FutureTask<>(()->{
            return 1024;
        });

    }


}
