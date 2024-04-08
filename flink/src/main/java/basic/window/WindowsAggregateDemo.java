package basic.window;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  窗口函数  增量聚合的aggregrate的案例  未测试
 */
public class WindowsAggregateDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("10.0.0.84", 1111).map(new WaterSenorMapFunction());
        KeyedStream<WaterSenor, String> dataSourceKeyby = dataSource.keyBy(WaterSenor::getId);
        //窗口分配器
        WindowedStream<WaterSenor, String, TimeWindow> window = dataSourceKeyby.window(TumblingProcessingTimeWindows.of(Time.seconds(5)));
        //aggregate也是每来一条数据就会调用一次add方法，和reduce不同的是，aggregate在第一条数据来的时候也会被调用，但是有一个累加器的初始值
        SingleOutputStreamOperator<String> aggregate = window.aggregate(new AggregateFunction<WaterSenor, Integer, String>() {
            @Override
            //创建累加器，初始化累加器
            public Integer createAccumulator() {
                System.out.println("创建累加器");
                return 0;
            }
            @Override
            //聚合逻辑
            public Integer add(WaterSenor value, Integer accumulator) {
                System.out.println("调用add方法,value="+value);
                return accumulator + value.getVc();
            }
            @Override
            //获取最终结果，窗口触发的时候输出
            public String getResult(Integer accumulator) {
                System.out.println("调用getResult方法");
                return accumulator.toString();
            }
            @Override
            //这个merge方法只有会话窗口才会用到
            public Integer merge(Integer a, Integer b) {
                System.out.println("调用merge方法");
                return null;
            }
        });
        aggregate.print();
        env.execute();
    }


}
