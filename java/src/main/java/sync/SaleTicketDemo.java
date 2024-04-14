package sync;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/14 23:47
 * @description： 使用synchronized关键字，实现3个售票员卖30张票
 */
// 1.创建资源类，定义属性和方法，在这个例子中，资源是票，方法是卖
class Ticket{
    private int num = 30;
    synchronized void sale(){
        if(num > 0){
            System.out.println(Thread.currentThread().getName()+"已经卖出了第"+num--+"号票，还剩"+num+"张票");
        }
    }
}
public class SaleTicketDemo {
    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        //2. 创建多个线程，调用资源类的方法
        new Thread(() -> {
            for (int i =0;i<40;i++){
                ticket.sale();
            }
        },"aa").start();
        new Thread(() -> {
            for (int i =0;i<40;i++){
                ticket.sale();
            }
        },"bb").start();
        new Thread(() -> {
            for (int i =0;i<40;i++){
                ticket.sale();
            }
        },"cc").start();
    }
}
