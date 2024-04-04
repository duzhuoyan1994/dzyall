package basic.sink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.time.Duration;
import java.time.ZoneId;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/4 23:32
 * @description： 文件sink的案例
 */
public class CusFileSink {

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
        //forRowFormat：一行写入   forBulkFormat：批量写入
        FileSink<String> fileSink = FileSink.<String>forRowFormat(new Path("E:\\flink_test\\bbb"), new SimpleStringEncoder<>())
                //输出文件的一些配置，比如前缀和后缀,注意输出文件的前缀和后缀
                .withOutputFileConfig(OutputFileConfig.builder().withPartPrefix("du_").withPartSuffix(".log").build())
                //按照目录分桶
                .withBucketAssigner(new DateTimeBucketAssigner<>("yyyy-MM-dd HH",ZoneId.systemDefault()))
                //文件滚动策略
                .withRollingPolicy(DefaultRollingPolicy.builder().withRolloverInterval(Duration.ofSeconds(10)).withMaxPartSize(new MemorySize(1024*1024)).build())
                .build();

        dataSource.sinkTo(fileSink);
        env.execute();


    }

}
