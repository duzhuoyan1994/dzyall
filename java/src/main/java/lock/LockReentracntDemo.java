package lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 17:51
 * @description： local 的可重入锁的demo演示
 */
public class LockReentracntDemo {
    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        new Thread(()->{
            try{
                lock.lock();
                System.out.println(Thread.currentThread().getName()+" 外层");

                try{
                    lock.lock();
                    System.out.println(Thread.currentThread().getName()+" 内层");
                }finally {
                    lock.unlock();
                }

            }finally {
                lock.unlock();
            }
        },"t1").start();

    }

}
