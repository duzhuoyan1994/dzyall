package readwrite;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/17 12:09
 * @description： 读写锁的案例演示，多线程操作map
 */
class MyCache{
    private volatile Map<String,Object> map = new HashMap<>();

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    //放数据
    void put(String key, Object value)  {
        //添加写锁
        lock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()+" 正在进行写操作 " + key);
            TimeUnit.MICROSECONDS.sleep(300);
            map.put(key, value);
            System.out.println(Thread.currentThread().getName()+" 写完了 "+ key);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.writeLock().unlock();
        }
    }

    Object get(String key){
        //添加读锁
        lock.readLock().lock();
        Object result = null;
        try {
            System.out.println(Thread.currentThread().getName()+" 正在读操作 "+key );
            TimeUnit.MICROSECONDS.sleep(300);
            result = map.get(key);
            System.out.println(Thread.currentThread().getName()+" 取完了 " + key);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.readLock().unlock();
        }
        return result;
    }
}

public class ReadWriteDemo {
    public static void main(String[] args) throws InterruptedException {
        MyCache cache = new MyCache();
        //创建线程放数据
        for (int i = 1; i <=5 ; i++) {
            final int num = i;
            new Thread(()->{
                cache.put(num+"",num);
            },String.valueOf(i)).start();
        }
        TimeUnit.MICROSECONDS.sleep(300);
        //创建线程取数据
        for (int i = 1; i <=5 ; i++) {
            final int num = i;
            new Thread(()->{
                cache.get(num+"");
            },String.valueOf(i)).start();
        }
    }
}
