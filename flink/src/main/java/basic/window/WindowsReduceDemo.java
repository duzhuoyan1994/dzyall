package basic.window;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.*;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  窗口函数  增量聚合的reduce的案例  未测试
 */
public class WindowsReduceDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("10.0.0.84", 1111).map(new WaterSenorMapFunction());
        KeyedStream<WaterSenor, String> dataSourceKeyby = dataSource.keyBy(WaterSenor::getId);
        //窗口分配器
        WindowedStream<WaterSenor, String, TimeWindow> window = dataSourceKeyby.window(TumblingProcessingTimeWindows.of(Time.seconds(5)));
        //窗口函数  窗口流在调用普通的方法之后又变成了一个普通的stream
        SingleOutputStreamOperator<WaterSenor> reduce = window.reduce(new ReduceFunction<WaterSenor>() {
            @Override
            public WaterSenor reduce(WaterSenor value1, WaterSenor value2) throws Exception {
                //观察reduce是什么时候触发的
                //可以观察到在每个窗口中，窗口的第一条数据是不用调用reduce的，后面每来一条数据会计算一次，调用一次reduce
                //每调用一次不会输出，最后窗口触发计算的时候才会输出最终的结果
                //reduce的缺陷：输入，中间累加和输出的类型是一样的，如果想要都不一样可以使用aggregate
                System.out.println("value1=" + value1 + ",value2=" + value2);
                return new WaterSenor(value1.getId(), value2.getTs(), value1.getVc() + value2.getVc());
            }
        });
        reduce.print();
        env.execute();
    }


}
