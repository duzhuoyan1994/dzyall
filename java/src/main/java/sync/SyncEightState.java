package sync;

import java.util.concurrent.TimeUnit;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/16 15:59
 * @description： synchronized  八种锁的情况，主要包括锁的是什么，锁的范围多大
 */

class Phone{
    synchronized static void sendSMS() throws Exception{
        //停留4s
      TimeUnit.SECONDS.sleep(4);
        System.out.println("------sendSMS");
    }
    synchronized void sendEmail() throws Exception{
        System.out.println("------sendEmail");
    }
    public void getHello(){
        System.out.println("------getHello");
    }
}

/**
 *  synchronized 8中锁的情况
 *  1.标准访问，先打印短信还是邮件
 * ------sendSMS
 * ------sendEmail
 *  2.停4s在短信方法内，先打印短信还是邮件
 * ------sendSMS
 * ------sendEmail
 * 3.停4s在短信方法内，新增普通的hello方法，是先打短信还是hello
 * ------getHello
 * ------sendSMS
 * 4.停4s在短信方法内，现在有两部手机，先打印短信还是邮件
 * ------sendEmail
 * ------sendSMS
 * 5.停4s在短信方法内，两个静态同步方法，1部手机，先打印短信还是邮件
 * ------sendSMS
 * ------sendEmail
 * 6.停4s在短信方法内，两个静态同步方法，2部手机，先打印短信还是邮件
 * ------sendSMS
 * ------sendEmail
 * 7.停4s在短信方法内，1个静态方法，一个普通同步方法，1部手机，先打印短信还是邮件
 * ------sendEmail
 * ------sendSMS
 * 8.停4s在短信方法内，1个静态方法，一个普通同步方法，1部手机，先打印短信还是邮件
 * ------sendEmail
 * ------sendSMS
 */

public class SyncEightState {
    public static void main(String[] args) throws InterruptedException {
        Phone phone = new Phone();
        Phone phone2 = new Phone();

        new Thread(()->{
            try {
                phone.sendSMS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"aa").start();

        Thread.sleep(100);

        new Thread(()->{
            try {
                phone.sendEmail();
//                phone.getHello();
//                phone2.sendEmail();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"aa").start();

    }

}
