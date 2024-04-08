package basic.window;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:52
 * @description： flink  窗口函数的api的一个演示的demo
 */
public class WindowsApiDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataGeneratorSource<String> dataSrouce = new DataGeneratorSource<>(new GeneratorFunction<Long, String>() {
            @Override
            public String map(Long value) throws Exception {
                return "number_" + value;
            }
        }, 10, RateLimiterStrategy.perSecond(5), Types.STRING);
        DataStreamSource<String> dataSource = env.fromSource(dataSrouce, WatermarkStrategy.noWatermarks(), "dategen_source");
        KeyedStream<String, String> dataSourceKeyby = dataSource.keyBy(line -> line.split("_")[0]);

        //1.指定窗口分配器：指定用哪一种窗口，时间/计数/滚动/滑动/会话等
        //1.1 没有keyby的窗口 窗口内的所有数据会进入同一个子任务，并行度只能为1，所有数据都在一个窗口
        //dataSource.windowAll()
        //1.2 经过keyby的窗口  每个key都经过了一组窗口，各自独立的进行计算
        //dataSourceKeyby.window()
        dataSourceKeyby.window(TumblingEventTimeWindows.of(Time.seconds(10))); //滚动窗口 10s
        dataSourceKeyby.window(SlidingProcessingTimeWindows.of(Time.seconds(10),Time.seconds(2))); // 滑动窗口 窗口长度10s，步长是2s
        dataSourceKeyby.window(ProcessingTimeSessionWindows.withGap(Time.seconds(5))); //会话窗口，间隔5s
        dataSourceKeyby.countWindow(5);  //滚动计数窗口，每5个数据作为一个窗口
        dataSourceKeyby.countWindow(5,2);  //滑动计数窗口
        dataSourceKeyby.window(GlobalWindows.create());  //全局窗口，计数窗口的底层就是调用的这个窗口，需要自定义一个触发器


        // 2 指定窗口函数，窗口内数据的计算逻辑
        WindowedStream<String, String, TimeWindow> dataWindow = dataSourceKeyby.window(TumblingEventTimeWindows.of(Time.seconds(10)));
        //增量聚合：来一条数据，计算一条数据，窗口触发的时候输出计算结果
//        dataWindow.reduce();
//        dataWindow.aggregate();

        //全窗口函数：数据来了不计算，存起来，窗口触发的时候才开始计算并且输出结果
        //而且全窗口函数是可以取到上下文的一些信息，比如窗口的触发时间，开始和结束时间等等
//        dataWindow.process();
//        dataWindow.apply();

    }


}
