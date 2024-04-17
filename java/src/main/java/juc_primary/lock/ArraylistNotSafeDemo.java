package juc_primary.lock;

import java.util.*;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/15 22:35
 * @description： 多线程并发时，线程不安全的集合的演示案例，
 *                使用Arraylist做演示，Arraylist是线程不安全的
 */
public class ArraylistNotSafeDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        //解决方案一
//        List<String> list = new Vector<>();
        //解决方案二
//        List<String> list = Collections.synchronizedList(new ArrayList<>());
        //解决方案三
//        List<String> list = new CopyOnWriteArrayList();


        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                //向集合中添加元素
                list.add(UUID.randomUUID().toString().substring(0,8));
                //从集合中获取内容
                System.out.println(list.toString()); //可以看到在并发的情况下，获取的时候会出现ConcurrentModificationException异常
            },String.valueOf(i)).start();
        }
    }
}
