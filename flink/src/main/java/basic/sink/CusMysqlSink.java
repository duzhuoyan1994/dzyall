package basic.sink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 11:11
 * @description： fink mysql sink的案例演示(未测试)
 */
public class CusMysqlSink {
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
        SinkFunction<String> jdbcSink = JdbcSink.sink(
                //sql语句
                "sql语句",
                //预编译对象
                new JdbcStatementBuilder<String>() {
                    @Override
                    public void accept(PreparedStatement preparedStatement, String s) throws SQLException {
                        preparedStatement.setString(1, s.split("_")[0]);
                        preparedStatement.setString(2, s.split("_")[1]);
                    }
                },
                //jdbc的一些执行参数
                JdbcExecutionOptions.builder()
                        .withMaxRetries(2)
                        .withBatchSize(100)
                        .withBatchIntervalMs(3000)
                        .build(),
                //jdbc的连接参数
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("")
                        .withUsername("")
                        .withPassword("")
                        //这个参数解决一个长连接的问题，在无界流中，需要连接一直存在，但是mysql内部如果这个链接默认超过8个小时不用的话，那么就会回收，到时候就会报错。
                        .withConnectionCheckTimeoutSeconds(60)
                        .build()
        );

        dataSource.addSink(jdbcSink);
        env.execute();

    }
}
