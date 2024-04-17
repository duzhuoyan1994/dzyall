package juc_primary.blockqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/17 17:30
 * @description：  阻塞队列的演示案例 demo
 */
public class BlockingQueueDemo {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Object> queue = new ArrayBlockingQueue<>(3);
        //第一组：异常的添加和取出
//        System.out.println(queue.add("a"));
//        System.out.println(queue.add("b"));
//        System.out.println(queue.add("c"));
//        System.out.println(queue.element());
//
//        System.out.println(queue.add("w"));
//        System.out.println(queue.remove());
//        System.out.println(queue.remove());
//        System.out.println(queue.remove());
//        System.out.println(queue.remove());
        //第二组：特殊值的演示
//        System.out.println(queue.offer("a"));
//        System.out.println(queue.offer("b"));
//        System.out.println(queue.offer("c"));
//        System.out.println(queue.offer("ww"));
//
//        System.out.println(queue.poll());
//        System.out.println(queue.poll());
//        System.out.println(queue.poll());
//        System.out.println(queue.poll());

        //第三组：阻塞的情况
//        queue.put("a");
//        queue.put("b");
//        queue.put("c");
//        queue.put("w"); //这会阻塞
//
//        queue.take();
//        queue.take();
//        queue.take();
//        queue.take();  //这会阻塞

        //第四组 阻塞超时
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");
        queue.offer("w",3L, TimeUnit.SECONDS);

    }


}
