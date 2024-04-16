package sync;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 17:37
 * @description：  synchronized 的可重入锁的demo演示
 */
public class SycnReentractLockDemo {

    private synchronized void add(){
        add();
    }

    public static void main(String[] args) {
        Object o = new Object();
        //这个也可以看出来，再次调用同步的add方法的时候，还是可以调用，因此sync是可重入锁
        new SycnReentractLockDemo().add();
//        new Thread(()->{
//            //这个方法可以看到虽然中层和内层有锁，但是拿到外层锁的线程还是可以进去，这个就是可重入锁
//            synchronized (o){
//                System.out.println(Thread.currentThread().getName()+" 外层");
//                synchronized (o){
//                    System.out.println(Thread.currentThread().getName()+" 中层");
//                    synchronized (o){
//                        System.out.println(Thread.currentThread().getName()+" 内层");
//                    }
//                }
//            }
//        },"t1").start();


    }


}
