package basic.state;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
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
 * @description： 键控状态(keyby之后的)  ValueState  案例演示   连续两个传感器的水位值的差值大于10就报警
 */
public class KeyedValueStateDemo {

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
                      //必须要在open方法去初始化状态，不能直接new
                      public void open(Configuration parameters) throws Exception {
                          //从上下文中获取状态，需要传一个状态描述器，两个参数，一个名字，一个状态的类型。
                          lastVcState = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("lastVcState", Types.INT));
                      }
                      @Override
                    public void processElement(WaterSenor value, Context ctx, Collector<String> out) throws Exception {
                        //1.取出上一条数据的水位值
                        int lastVc = lastVcState.value() == null ? 0 : lastVcState.value();
                        //2.求差值的绝对值，判断是否超过10
                          if (Math.abs(value.getVc() - lastVc) > 10) {
                              out.collect("传感器id="+value.getId()+"，当前水位值="+value.getVc()+"，与上一条水位值="+lastVc+"，相差超过10！！");
                          }
                        //3.保存更新自己的水位值
                        lastVcState.update(value.getVc());
                    }
                }).print();

        env.execute();

    }

}
