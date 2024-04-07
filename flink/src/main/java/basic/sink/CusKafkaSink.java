package basic.sink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/4 23:54
 * @description： kafka sink的案例  未测试
 */
public class CusKafkaSink {

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

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                //kafka的broker地址
                .setBootstrapServers("127.0.0.1:9092")
                //指定序列化器，topic名称，具体的序列化
                .setRecordSerializer(KafkaRecordSerializationSchema.<String>builder()
                        .setTopic("")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                //写到kafka的一致性级别
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                //如果是精确一次，必须设置事务的前缀
                .setTransactionalIdPrefix("du-")
                //如果是精确一次，那么必须要设置超时时间，大于checkpoint时间间隔，小于max(15分钟)
                .setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,10*60*1000+"")
                .build();

        dataSource.sinkTo(kafkaSink);
        env.execute();


    }
}
