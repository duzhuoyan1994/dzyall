package basic.watermark;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  watermark 自定义断点式watermark 案例  未测试  断点式指的是在onEvent中去发射watermark，而不是onPeriodicEmit在这个中去发射
 */
public class WatermarkPuncateDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSenor> dataSource = env.
                socketTextStream("10.0.0.84", 1111).
                map(new WaterSenorMapFunction());
        //指定watermark策略
        WatermarkStrategy<WaterSenor> strategy = WatermarkStrategy.
                //自定义watermark策略，传入一个watermark的生成器的选择器
                <WaterSenor>forGenerator(new WatermarkGeneratorSupplier<WaterSenor>() {
                    @Override
                    public WatermarkGenerator<WaterSenor> createWatermarkGenerator(Context context) {
                        //返回我们自定义的watermark的生成器
                        return new MyPeriodWatermarkGenerator<>(3*1000);
                    }
                })
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

        sourceWatermark.keyBy(WaterSenor::getId)
             //使用watermark的时候，这块的滚动窗口的时间一定是事件时间，不再是之前的处理时间了，这个要注意
            .window(TumblingEventTimeWindows.of(Time.seconds(5)))
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
        }).print();
        env.execute();
    }

    public static class MyPeriodWatermarkGenerator<T> implements WatermarkGenerator<T> {
        //乱序等待时间
        private long delayTs;
        //用来保存当前为止最大的事件时间
        private long maxTs;
        MyPeriodWatermarkGenerator(long delayTs) {
            this.delayTs = delayTs;
            this.maxTs = Long.MIN_VALUE + this.delayTs + 1;
        }
        @Override
        //每条数据来都会调用一次，主要用来提取最大的事件时间保存下来，并发射watermark
        //eventTimestamp  这个是提取到的数据中包含的事件时间
        public void onEvent(T event, long eventTimestamp, WatermarkOutput output) {
            maxTs = Math.max(maxTs,eventTimestamp);
            output.emitWatermark(new Watermark(maxTs-delayTs-1));
            System.out.println("调用了onEvent方法，获取目前为止的最大时间戳="+maxTs+",watermark="+(maxTs-delayTs-1));
        }
        @Override
        //断点式的时候不需要这个方法，这个是周期性的时候使用的
        public void onPeriodicEmit(WatermarkOutput output) {
        }
    }
}
