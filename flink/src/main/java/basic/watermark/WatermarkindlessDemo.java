package basic.watermark;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  watermark 多个并行度的watermark取最小值，
 * 但是如果多个并行度中，有一个并行度迟迟不来数据，那么watermark没办法更新，窗口不会触发，
 * 这个时候就需要设置空闲等待时间，表示多长时间不来数据，那么就抛弃这个并行度时间，触发窗口了
 * 未测试
 */
public class WatermarkindlessDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        //自定义分区器， 数据%分区器，如果只输入奇数，那么只会去往map的一个子任务
        SingleOutputStreamOperator<Integer> socketDS = env
                .socketTextStream("10.0.0.84", 1111)
                .partitionCustom(new MyPartitoner(), r -> r)
                .map(Integer::parseInt)
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<Integer>forMonotonousTimestamps()
                        .withTimestampAssigner((r, ts) -> r * 1000)
                        //表示空闲等待时间5s
                        .withIdleness(Duration.ofSeconds(5)));
        // 分成两组，奇数，偶数，开10s的事件时间滚动窗口
        socketDS.keyBy(r->r%2).window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .process(new ProcessWindowFunction<Integer, String, Integer, TimeWindow>() {
                    @Override
                    public void process(Integer integer, Context context, Iterable<Integer> elements, Collector<String> out) throws Exception {
                        long startTs = context.window().getStart();
                        long endTs = context.window().getEnd();
                        String windowStart = DateFormatUtils.format(startTs, "yyyy-MM-dd HH:mm:ss");
                        String windowEnd = DateFormatUtils.format(endTs, "yyyy-MM-dd HH:mm:ss");

                        long count = elements.spliterator().estimateSize();
                        out.collect("key=" + integer + "的窗口[" + windowStart + "," + windowEnd + ")包含" + count + "条数据==>" + elements.toString());
                    }
                }).print();



        env.execute();
    }


    public static class MyPartitoner implements Partitioner<String>{
        @Override
        public int partition(String key, int numPartitions) {
            return Integer.parseInt(key) % numPartitions;
        }
    }

}
