package sync;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/15 21:34
 * @description： 线程间的通信演示案例，主要方法是 wait  和  notify这两个方法
 *          在share中的incre和decre方法中，让线程等待的话，必须要使用while循环去实现，如果用if的话
 *          会出现虚假唤醒，也就是只有第一次判断条件了，后面的没有判断条件，因为wait是在哪睡的就在哪醒
 */
class Share{
    //初始值
    private int number = 0;
    //+1的方法
    synchronized void incre() throws InterruptedException {
//      if(number!=0){  如果使用if，那么会出现虚假唤醒的情况
        while(number!=0){
            this.wait();
        }
        number++;
        System.out.println(Thread.currentThread().getName()+" :: "+number);
        this.notifyAll();
    }
    synchronized void decre() throws InterruptedException {
//      if(number!=1){  同理，if会出现虚假唤醒
        while(number!=1){
            this.wait();
        }
        number--;
        System.out.println(Thread.currentThread().getName()+" :: "+number);
        this.notifyAll();
    }
}
public class WaitNofiSynDemo {
    public static void main(String[] args) {
        Share share = new Share();
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                try {
                    share.incre();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"aa").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                try {
                    share.decre();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"bb").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                try {
                    share.incre();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"cc").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                try {
                    share.decre();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"dd").start();
    }
}
