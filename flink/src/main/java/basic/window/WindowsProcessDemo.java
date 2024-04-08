package basic.window;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  窗口函数  全窗口函数 process   窗口触发的时候才会进行计算  未测试
 */
public class WindowsProcessDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("10.0.0.84", 1111).map(new WaterSenorMapFunction());
        KeyedStream<WaterSenor, String> dataSourceKeyby = dataSource.keyBy(WaterSenor::getId);
        //窗口分配器
        WindowedStream<WaterSenor, String, TimeWindow> window = dataSourceKeyby.window(TumblingEventTimeWindows.of(Time.seconds(5)));
        //process 这个是全窗口，也就是说不会来一条计算一条，而是先降数据全部缓存起来，窗口的触发时间到了之后，然后再进行全部计算
        SingleOutputStreamOperator<String> process = window.process(new ProcessWindowFunction<WaterSenor, String, String, TimeWindow>() {
            @Override
            public void process(String s, Context context, Iterable<WaterSenor> elements, Collector<String> out) throws Exception {
                //通过context的上下文对象，可以拿到window这个对象，还有一些侧输出流，状态等东西
                long startTs = context.window().getStart();
                long endTs = context.window().getEnd();
                String windowStart = DateFormatUtils.format(startTs, "yyyy-MM-dd HH:mm:ss");
                String windowEnd = DateFormatUtils.format(endTs, "yyyy-MM-dd HH:mm:ss");

                long count = elements.spliterator().estimateSize();
                out.collect("key=" + s + "的窗口[" + windowStart + "," + windowEnd + "]包含" + count + "条数据==>" + elements.toString());
            }
        });
        process.print();
        env.execute();
    }


}
