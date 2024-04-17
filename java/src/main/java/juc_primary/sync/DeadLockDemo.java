package juc_primary.sync;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 18:04
 * @description：  死锁的demo
 */
public class DeadLockDemo {
    private static Object a = new Object();
    private static Object b = new Object();
    public static void main(String[] args) {
        new Thread(()->{
            synchronized (a){
                System.out.println(Thread.currentThread().getName()+"持有锁a，试图获取锁b");

                synchronized (b){
                    System.out.println(Thread.currentThread().getName()+" 获取到锁b");
                }
            }
        },"a").start();

        new Thread(()->{
            synchronized (b){
                System.out.println(Thread.currentThread().getName()+"持有锁b，试图获取锁a");

                synchronized (a){
                    System.out.println(Thread.currentThread().getName()+" 获取到锁a");
                }
            }
        },"b").start();
    }
}
