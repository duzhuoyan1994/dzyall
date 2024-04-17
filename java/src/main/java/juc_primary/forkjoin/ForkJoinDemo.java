package juc_primary.forkjoin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/17 21:41
 * @description：  ForkJoin 分支合并的代码demo
 *                  实现一个1到100的累加，使用分支合并的方法。
 *                  大概逻辑是只要两个值相差大于10，那我就拆分，如果小于等于10，那么就加起来
 *                  举个例子，比如1-100，大于10，那么拆分成1-50和51-100，然后两边再递归拆分，直至一个区间段的数据小于10.
 */
class MyTask extends RecursiveTask<Integer> {
    //拆分差值不能超过10
    private static final Integer VALUE = 10;
    private int begin; //拆分开始值
    private int end;  //拆分结束值
    private int result;  //返回结果
    MyTask(int begin, int end){
        this.begin = begin;
        this.end = end;
    }
    //拆分和合并的过程
    @Override
    protected Integer compute() {
        //判断相加的两个数是否大于10
        if(end-begin<=VALUE){
            //相加操作
            for (int i = begin; i <=end ; i++) {
                result = result+i;
            }
        }else{
            //进一步做拆分
            //获取数据中间值
            int middle = (begin+end)/2;
            //拆分左边
            MyTask task1 = new MyTask(begin,middle);
            //拆分右边
            MyTask task2 = new MyTask(middle+1,end);
            //调用方法拆分
            task1.fork();
            task2.fork();
            //合并一个结果
            result = task1.join()+task2.join();
        }
        return result;
    }
}

public class ForkJoinDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //创建拆分任务对象
        MyTask myTask = new MyTask(1,100);
        //创建分支合并池对象
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ForkJoinTask<Integer> task = forkJoinPool.submit(myTask);
        Integer result = task.get();
        System.out.println(result);

        forkJoinPool.shutdown();


    }
}
