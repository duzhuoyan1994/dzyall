package basic.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/4 11:47
 * @description： 使用kafka自带的source读取kafka中的数据
 *                要注意的是在1.17.1中，读取FlinkKafkaConsumer的这个类已经准备废弃了，使用kafkasource了
 *
 */
public class CusKafkaSource {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("127.0.0.1:9092")
                .setGroupId("groupid")
                .setTopics("topic")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(),"kakfa-source").print();
        env.execute("kafka-source");


    }


}
