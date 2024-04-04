package basic.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/4 22:35
 * @description： flink中有一个内置的DataGen连接器，主要是用于生成一些随机数，用于在没有数据源的时候，进行任务及性能测试。
 */
public class CusDatagenSource {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        /**
         * 数据生成器的四个参数
         * 1.GeneratorFunction  生成什么样的数据类型和数据逻辑的函数
         * 2.Long类型的最大值，表示value从0开始自增到多少，有多少条数据。如果有多个并行度的话，value是总量。
         * 3.限速策略，比如每s生成几条数据
         * 4.返回的类型
         */
        DataGeneratorSource<String> dataSrouce = new DataGeneratorSource<>(new GeneratorFunction<Long, String>() {
            @Override
            public String map(Long value) throws Exception {
                return "number_" + value;
            }
        }, 10, RateLimiterStrategy.perSecond(10), Types.STRING);
        env.fromSource(dataSrouce, WatermarkStrategy.noWatermarks(),"dategen_source").print();
        env.execute();

    }

}
