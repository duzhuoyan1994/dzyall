package basic.state;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/9 22:41
 * @description： 键控状态(keyby之后的)  ListState  案例演示  每种传感器输出最高的三个水位值
 */
public class KeyedListValueStateDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSenor> dataSource = env.socketTextStream("", 1111).map(new WaterSenorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<WaterSenor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, ts) -> element.getTs() * 1000L));

        dataSource.keyBy(WaterSenor::getId)
                  .process(new KeyedProcessFunction<String, WaterSenor, String>() {
                      ListState<Integer> vcListState ;

                      @Override
                      public void open(Configuration parameters) throws Exception {
                          vcListState = getRuntimeContext().getListState(new ListStateDescriptor<Integer>("vcListState",Types.INT));
                      }

                      @Override
                      public void processElement(WaterSenor value, Context ctx, Collector<String> out) throws Exception {
                          //来一条，存到List状态中
                          vcListState.add(value.getVc());
                          //从list中拿出来，拷贝到一个list中排序，只留最大的三个
                          Iterable<Integer> vcListIt = vcListState.get();
                          ArrayList<Integer> vcList = new ArrayList<>();
                          for(Integer vc : vcListIt){
                              vcList.add(vc);
                          }
                          //对list进行降序排序
                          vcList.sort(((o1, o2) -> o2-o1));
                          //只保留最大的3个
                          if(vcList.size()>3) vcList.remove(3);
                          out.collect("传感器id为"+value.getId()+"，最大的三个水位值="+vcList.toString());
                          //更新list状态
                          vcListState.update(vcList);
                      }
                  }).print();

        env.execute();

    }

}
