package readwrite;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/17 16:59
 * @description：  读写锁降级的演示demo，写锁降级为读锁。
 */
public class WriteToReadLockDemo {
    public static void main(String[] args) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        //读锁
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        //写锁
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        //锁降级，如果先获取读锁，再获取写锁，那锁不会升级，那么会一直卡住
        //获取写锁
        writeLock.lock();
        System.out.println("duzhuoyan-----write");
        //获取读锁
        readLock.lock();
        System.out.println("duzhuoyan-----read");
        //释放写锁
        writeLock.unlock();
        //释放读锁
        readLock.unlock();
    }

}
