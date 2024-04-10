package basic.state;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/10 18:07
 * @description：  checkpoint的配置  案例
 */
public class CheckpointConfigDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //checkpoint  config
        //启用检查点，默认是barrier对齐的
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        //指定checkpoint的存储位置 存储到hdfs，如果在本地需要导入hadoop的依赖，但是不要打包到生产，有可能会jar冲突，还需要指定访问用户名
        System.setProperty("HADOOP_USER_NAME","USER_NAME");
        env.getCheckpointConfig().setCheckpointStorage("hdfs://");
        //超时时间 默认是10分钟
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        //同时运行中的checkpoint的最大数量
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        //最小等待间隔 上一轮checkpoint结束到下一轮checkpoint开始之间的间隔
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000);
        //保存取消作业时，checkpoint的数据保留在外部系统
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);
        //允许checkpoint连续失败的次数，默认是0，表示checkpoint一失败，job就挂掉
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(10);


        env.socketTextStream("",1111)
                .map(line -> Tuple2.of(line.split(" ")[0],line.split(" ")[0]))
                .returns(Types.TUPLE(Types.STRING,Types.STRING))
                .keyBy(value -> value.f0)
                .sum(1)
                .print();
        env.execute();
    }

}
