package basic.state;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.AggregatingState;
import org.apache.flink.api.common.state.AggregatingStateDescriptor;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/9 22:41
 * @description： 键控状态(keyby之后的)  AggregateState  案例演示  统计每种传感器的平均水位
 */
public class KeyedAggregateStateDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("", 1111).map(new WaterSenorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<WaterSenor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, ts) -> element.getTs() * 1000L));

        dataSource.keyBy(WaterSenor::getId)
                  .process(new KeyedProcessFunction<String, WaterSenor, Object>() {
                      AggregatingState<Integer,Double> vcAggState;

                      @Override
                      public void open(Configuration parameters) throws Exception {
                          vcAggState =  getRuntimeContext().getAggregatingState(
                                  new AggregatingStateDescriptor<Integer, Tuple2<Integer,Integer>, Double>(
                                          "vcAggState",
                                          new AggregateFunction<Integer, Tuple2<Integer, Integer>, Double>() {
                                              @Override
                                              public Tuple2<Integer, Integer> createAccumulator() {
                                                    return Tuple2.of(0,0);
                                              }
                                              @Override
                                              public Tuple2<Integer, Integer> add(Integer value, Tuple2<Integer, Integer> accumulator) {
                                                  return Tuple2.of(accumulator.f0+value,accumulator.f1+1);
                                              }
                                              @Override
                                              public Double getResult(Tuple2<Integer, Integer> accumulator) {
                                                  return accumulator.f0*1D/accumulator.f1;
                                              }
                                              @Override
                                              public Tuple2<Integer, Integer> merge(Tuple2<Integer, Integer> a, Tuple2<Integer, Integer> b) {
                                                  //return Tuple2.of(a.f0+b.f0,a.f1+b.f1)
                                                  return null;
                                              }
                                          },Types.TUPLE(Types.INT,Types.INT)
                                  )
                          );
                      }
                      @Override
                      public void processElement(WaterSenor value, Context ctx, Collector<Object> out) throws Exception {
                          //将水位值添加到状态中
                          vcAggState.add(value.getVc());
                          //从聚合状态中获取水位值
                          Double vcAvg = vcAggState.get();
                          out.collect("传感器id="+value.getId()+"，平均水位值为"+vcAvg);
                      }
                  }).print();
        env.execute();

    }

}
