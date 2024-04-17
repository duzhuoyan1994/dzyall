package juc_primary.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/15 16:53
 * @description： ReentrantLock 使用这个可重入锁实现 3个人卖票30张
 */
class LockTicket{
    private int num = 30;
    private ReentrantLock lock = new ReentrantLock();
    void sale(){
        lock.lock();
        try{
            if(num > 0){
                System.out.println(Thread.currentThread().getName()+"已经卖出了第"+num--+"号票，还剩"+num+"张票");
            }
        }finally {
            lock.unlock();
        }
    }
}
public class LockSaleTicketDemo {
    public static void main(String[] args) {
        LockTicket loclTicket = new LockTicket();
        new Thread(() -> {
            for (int i =0;i<40;i++){
                loclTicket.sale();
            }
        },"aa").start();

        new Thread(() -> {
            for (int i =0;i<40;i++){
                loclTicket.sale();
            }
        },"bb").start();

        new Thread(() -> {
            for (int i =0;i<40;i++){
                loclTicket.sale();
            }
        },"cc").start();
    }
}
