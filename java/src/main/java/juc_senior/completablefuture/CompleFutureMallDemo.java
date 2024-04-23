package juc_senior.completablefuture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/23 22:22
 * @description： 一个案例需求
 * 同一款产品，同时搜索出同款产品在各大电商平台的售价
 * 同一款产品，同时搜索出本产品在同一个电商平台下，各个入驻卖家售价是多少
 * 技术要求：
 * 函数式编程，链式编程，Stream流式计算
 */
public class CompleFutureMallDemo {

    static List<NetMall> list = Arrays.asList(
            new NetMall("jd"),
            new NetMall("dangdang"),
            new NetMall("taobao"),
            new NetMall("tianmao"),
            new NetMall("pdd")
    );

    //查询逻辑和过程，首先是没有启用多线程异步逻辑的，一个一个的查询
    public static List<String> getPrice(List<NetMall> list, String productName){
        return list.stream()
                .map(netMall -> String.format("%s in %s price is %.2f",productName,netMall.getNetMallName(),netMall.calPrice(productName)))
                .collect(Collectors.toList());
    }

    //查询逻辑和过程，使用异步开启线程提交任务，提升性能
    public static List<String> getPriceCompleFuture(List<NetMall> list, String productName){
        //在这个流式处理中，如果把collect和stream注释掉，那么还是会变成串行的，需要深入研究是为什么
        return list.stream().map(netMall -> CompletableFuture.supplyAsync(()-> String.format("%s in %s price is %.2f",productName,netMall.getNetMallName(),netMall.calPrice(productName))))
                .collect(Collectors.toList())
                .stream()
                .map(s -> s.join())
                .collect(Collectors.toList());

    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        List<String> res = getPrice(list, "mysql");
        for (String ele:res) {
            System.out.println(ele);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("------costTime: " + (endTime - startTime) + " 毫秒");

        System.out.println("--------------------------------");

        long startTime2 = System.currentTimeMillis();
        List<String> resComple = getPriceCompleFuture(list, "mysql");
        for (String ele: resComple) {
            System.out.println(ele);
        }
        long endTime2 = System.currentTimeMillis();
        System.out.println("------costTime: " + (endTime2 - startTime2) + " 毫秒");
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //链式编程的注解
class NetMall{  //模拟电商网站这个类，有网站的名字和提供一个方法，通过产品可以查出来价格
    private String netMallName;
    //通过产品查出价格
    public double calPrice(String productName){
        //模拟业务过程，花费了1s来查出需要的数据
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回产品的价格
        return ThreadLocalRandom.current().nextDouble()*2 + productName.charAt(0);
    }
}
