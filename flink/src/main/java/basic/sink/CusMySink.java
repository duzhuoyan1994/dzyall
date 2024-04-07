package basic.sink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:33
 * @description： 自定义sink的逻辑,一定注意自定义的时候，如果需要创建外部链接，要用richfunciton类
 */
public class CusMySink {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataGeneratorSource<String> dataSrouce = new DataGeneratorSource<>(new GeneratorFunction<Long, String>() {
            @Override
            public String map(Long value) throws Exception {
                return "number_" + value;
            }
        }, 10, RateLimiterStrategy.perSecond(5), Types.STRING);
        DataStreamSource<String> dataSource = env.fromSource(dataSrouce, WatermarkStrategy.noWatermarks(), "dategen_source");

        dataSource.addSink(new MySink());
        env.execute();
    }

    public static class MySink extends RichSinkFunction<String> {

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            //在这里创建连接
        }

        @Override
        public void close() throws Exception {
            super.close();
            //在这里关闭连接
        }

        //sink的核心逻辑，invoke指的是调用的意思
        @Override
        public void invoke(String value, Context context) throws Exception {
            //写出逻辑
            //这个方法是来一条，调用一次，不适合创建连接,因此重写的时候要使用rich函数
        }
    }
}
