package basic.state;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.IsolationLevel;

import java.time.Duration;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/13 23:35
 * @description： 从kafka读取数据，使用flink处理完成之后，发送到kafka，实现端到端的精确一次
 *                  flink读取kafka，可以精确一次
 *                  flink内部可以实现精确一次
 *                  flink发送kafka，使用2pc可以精确一次  综上：保证了端到端的精确一次
 *      还有一个点：kafka的消费者的默认隔离级别是读未提交，要保证kafka端到端的一致性，要将消费者的隔离级别设置成都已提交。
 *      .setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG,"real_committed")  kafka的source端设置读已提交
 */
public class KafkaEODDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        System.setProperty("HADOOP_USER_NAME","USER_NAME");
        env.getCheckpointConfig().setCheckpointStorage("hdfs://");
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);

        //kafka source
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("127.0.0.1:9092")
                .setGroupId("groupid")
                .setTopics("topic")
                .setStartingOffsets(OffsetsInitializer.earliest())

                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> source = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kakfa-source");

        //kakfa sink
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                //kafka的broker地址
                .setBootstrapServers("127.0.0.1:9092")
                //指定序列化器，topic名称，具体的序列化
                .setRecordSerializer(KafkaRecordSerializationSchema.<String>builder()
                        .setTopic("")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                //写到kafka的一致性级别，这个精确性就是保证了2pc的开启，保证了写kafka的精确一次
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                //如果是精确一次，必须设置事务的前缀
                .setTransactionalIdPrefix("du-")
                //如果是精确一次，那么必须要设置超时时间，大于checkpoint时间间隔，小于max(15分钟)
                .setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,10*60*1000+"")
                .build();

        source.sinkTo(kafkaSink);
        env.execute();
    }

}
