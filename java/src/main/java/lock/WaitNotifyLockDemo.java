package lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/15 21:47
 * @description：   线程间的通信问题
 *                  使用lock锁来演示wait和notify的方法，但是在lock中调用的方法不同，但是意义是一样的
 */
class LockShare{
    private int number = 0 ;
    private Lock lock = new ReentrantLock();
    //使用lock中的等待和唤醒需要从lock锁中获取到这个Condition对象，是通过这个对象来实现的
    private Condition condition = lock.newCondition();

    void incre(){
        lock.lock();
        try{
            while(number!=0){
                condition.await();
            }
            number++;
            System.out.println(Thread.currentThread().getName()+" :: "+number);
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    void decre(){
        lock.lock();
        try{
            while(number!=1){
                condition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName()+" :: "+number);
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}


public class WaitNotifyLockDemo {
    public static void main(String[] args) {
        LockShare lockShare = new LockShare();
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                lockShare.incre();
            }
        },"aa").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                lockShare.decre();
            }
        },"bb").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                lockShare.incre();
            }
        },"cc").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                lockShare.decre();
            }
        },"dd").start();
    }

}
