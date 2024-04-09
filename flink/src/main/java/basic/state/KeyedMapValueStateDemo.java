package basic.state;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/9 22:41
 * @description： 键控状态(keyby之后的)  MapState  案例演示  统计每种传感器每种水位值出现的次数
 */
public class KeyedMapValueStateDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("", 1111).map(new WaterSenorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<WaterSenor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, ts) -> element.getTs() * 1000L));

        dataSource.keyBy(WaterSenor::getId)
                  .process(new KeyedProcessFunction<String, WaterSenor, String>() {
                      MapState<Integer,Integer> vcCountMapState;

                      @Override
                      public void open(Configuration parameters) throws Exception {
                          vcCountMapState = getRuntimeContext().getMapState(new MapStateDescriptor<Integer, Integer>("vcCountMapState",Types.INT,Types.INT));
                      }
                      @Override
                      public void processElement(WaterSenor value, Context ctx, Collector<String> out) throws Exception {
                          //判断是否存在对应的key
                          Integer vc = value.getVc();
                          if(vcCountMapState.contains(vc)){
                              Integer count = vcCountMapState.get(vc);
                              vcCountMapState.put(vc,++count);
                          }else{
                              vcCountMapState.put(vc,1);
                          }
                          StringBuilder outStr = new StringBuilder();
                          outStr.append("传感器id为").append(value.getId()).append("\n");
                          for(Map.Entry<Integer,Integer> vcCount:vcCountMapState.entries()){
                              outStr.append(vcCount.toString()).append("\n");
                          }
                          outStr.append("==============");
                          out.collect(outStr.toString());
                      }
                  }).print();

        env.execute();

    }

}
