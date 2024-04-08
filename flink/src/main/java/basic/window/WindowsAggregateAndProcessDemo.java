package basic.window;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  窗口函数  增量聚合和全量聚合两个窗口结合使用  其实本质上就是调用了aggregate的一个重载的方法(reduce也是同理)，可以多传入一个ProcessWindowFunction函数
 */
public class WindowsAggregateAndProcessDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("10.0.0.84", 1111).map(new WaterSenorMapFunction());
        KeyedStream<WaterSenor, String> dataSourceKeyby = dataSource.keyBy(WaterSenor::getId);
        //窗口分配器
        WindowedStream<WaterSenor, String, TimeWindow> window = dataSourceKeyby.window(TumblingProcessingTimeWindows.of(Time.seconds(5)));
        //使用这个重载的方法的时候，要注意的是这个方法的数据流向第一个函数的输出，是第二个函数的输入，因为第一个函数是增量聚合，因此第二个函数肯定只接收一条数据
        SingleOutputStreamOperator<String> aggregate = window.aggregate(new MyAgg(), new MyProcess());
        aggregate.print();
        env.execute();
    }

    public static class MyAgg implements AggregateFunction<WaterSenor,Integer,String>{
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
    }
    //这的泛型的输入的参数取决于前一个函数的输出类型，这两个类型要保持一致
    public static class MyProcess extends ProcessWindowFunction<String,String,String,TimeWindow>{
        @Override
        public void process(String s, Context context, Iterable<String> elements, Collector<String> out) throws Exception {
            long startTs = context.window().getStart();
            long endTs = context.window().getEnd();
            String windowStart = DateFormatUtils.format(startTs, "yyyy-MM-dd HH:mm:ss");
            String windowEnd = DateFormatUtils.format(endTs, "yyyy-MM-dd HH:mm:ss");

            long count = elements.spliterator().estimateSize();
            out.collect("key=" + s + "的窗口[" + windowStart + "," + windowEnd + ")包含" + count + "条数据==>" + elements.toString());
        }
    }

}
