package basic.watermark;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  watermark 迟到数据处理 未测试
 * 迟到和乱序的区别，乱序是数据是无序的，但是事件时间差距不会很大，迟到指的是事件时间差距比较大
 */
public class WatermarkLatenessDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSenor> dataSource = env.
                socketTextStream("10.0.0.84", 1111).
                map(new WaterSenorMapFunction());

        //指定watermark策略
        WatermarkStrategy<WaterSenor> strategy = WatermarkStrategy.
                //乱序流执行watermark的生成,等待3s
                <WaterSenor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                //提取输入数据的那个属性作为事件时间
                .withTimestampAssigner(new SerializableTimestampAssigner<WaterSenor>() {
                    @Override
                    public long extractTimestamp(WaterSenor element, long recordTimestamp) {
                        System.out.println("shuju=" + element + ",recordTs=" + recordTimestamp);
                        //返回的时间戳是ms
                        return element.getTs() * 1000;
                    }
                });

        SingleOutputStreamOperator<WaterSenor> sourceWatermark = dataSource.assignTimestampsAndWatermarks(strategy);

        OutputTag<WaterSenor> lateTag = new OutputTag<>("late_data", Types.POJO(WaterSenor.class));
        SingleOutputStreamOperator<String> process = sourceWatermark.keyBy(WaterSenor::getId)
                //使用watermark的时候，这块的滚动窗口的时间一定是事件时间，不再是之前的处理时间了，这个要注意
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .allowedLateness(Time.seconds(5)) //允许5s的迟到数据
                .sideOutputLateData(lateTag) //迟到的数据，如果窗口已经关闭了，就放到侧输出流里面
                .process(new ProcessWindowFunction<WaterSenor, String, String, TimeWindow>() {
                    @Override
                    public void process(String s, Context context, Iterable<WaterSenor> elements, Collector<String> out) throws Exception {
                        long startTs = context.window().getStart();
                        long endTs = context.window().getEnd();
                        String windowStart = DateFormatUtils.format(startTs, "yyyy-MM-dd HH:mm:ss");
                        String windowEnd = DateFormatUtils.format(endTs, "yyyy-MM-dd HH:mm:ss");

                        long count = elements.spliterator().estimateSize();
                        out.collect("key=" + s + "的窗口[" + windowStart + "," + windowEnd + ")包含" + count + "条数据==>" + elements.toString());
                    }
                });
        process.print();
        process.getSideOutput(lateTag).printToErr();

        env.execute();
    }


}
