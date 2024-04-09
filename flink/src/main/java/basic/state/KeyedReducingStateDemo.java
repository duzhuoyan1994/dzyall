package basic.state;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Map;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/9 22:41
 * @description： 键控状态(keyby之后的)  ReduciongState  案例演示  统计每种传感器的水位和
 *    和ListState类似，只是liststate是把数据存起来，而reducingstate是将数据按照我们规定的逻辑聚合归约起来
 *    有点类似窗口中的增量聚合和全窗口函数
 */
public class KeyedReducingStateDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("", 1111).map(new WaterSenorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<WaterSenor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, ts) -> element.getTs() * 1000L));

        dataSource.keyBy(WaterSenor::getId)
                  .process(new KeyedProcessFunction<String, WaterSenor, String>() {
                      ReducingState<Integer> vcSumReductionState ;

                      @Override
                      public void open(Configuration parameters) throws Exception {
                          vcSumReductionState = getRuntimeContext().getReducingState(new ReducingStateDescriptor<Integer>(
                                  "vcSumReductionState",
                                  new ReduceFunction<Integer>() {
                                      @Override
                                      public Integer reduce(Integer value1, Integer value2) throws Exception {
                                          return value1 + value2;
                                      }
                                  },
                                  Types.INT));
                      }
                      @Override
                      public void processElement(WaterSenor value, Context ctx, Collector<String> out) throws Exception {
                          //来一条数据，添加到reduction状态里面
                          vcSumReductionState.add(value.getVc());
                          out.collect("传感器id为"+value.getId()+"，水位值总和="+vcSumReductionState.get());
                      }
                  }).print();

        env.execute();

    }

}
