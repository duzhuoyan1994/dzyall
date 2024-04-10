package basic.state;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/9 22:41
 * @description： 键控状态(keyby之后的)  利用valuestate这个状态演示 状态失效时间的一个案例   连续两个传感器的水位值的差值大于10就报警
 */
public class KeyedStateTTLDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("", 1111).map(new WaterSenorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<WaterSenor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, ts) -> element.getTs() * 1000L));

        dataSource.keyBy(WaterSenor::getId)
                  .process(new KeyedProcessFunction<String, WaterSenor, String>() {
                      ValueState<Integer> lastVcState;
                      @Override
                      public void open(Configuration parameters) throws Exception {
                          //创建statettlconfig
                          StateTtlConfig stateTTLConfig = StateTtlConfig.newBuilder(Time.seconds(5))
                                  //状态创建和写入会更新状态的失效时间，还有其他模式
                                  .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                                  //不返回过期的状态值，还有其他模式
                                  .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                                  .build();
                          //状态描述器启用TTL
                          ValueStateDescriptor<Integer> stateDescriptor = new ValueStateDescriptor<>("lastVcState", Types.INT);
                          stateDescriptor.enableTimeToLive(stateTTLConfig);
                          this.lastVcState = getRuntimeContext().getState(stateDescriptor);
                      }
                      @Override
                    public void processElement(WaterSenor value, Context ctx, Collector<String> out) throws Exception {
                        //先获取状态值，打印
                        int lastVc = lastVcState.value();
                        out.collect("key="+value.getId()+"，状态值="+lastVc);
                        //更新状态值
                        lastVcState.update(value.getVc());
                    }
                }).print();

        env.execute();
    }

}
