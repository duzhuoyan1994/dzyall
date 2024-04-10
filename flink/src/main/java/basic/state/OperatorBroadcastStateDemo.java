package basic.state;

import bean.WaterSenor;
import function.WaterSenorMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/10 11:37
 * @description：  算子状态（也就是没有经过keyby的），整个算子共享一个状态，广播状态 broadcast 的一个案例
 *          水位线超过指定的阈值发送告警，阈值可以动态的进行修改
 */
public class OperatorBroadcastStateDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //数据流
        SingleOutputStreamOperator<WaterSenor> dataSrouce =
                env.socketTextStream("", 1111).map(new WaterSenorMapFunction());
        //配置流（用来广播配置）
        DataStreamSource<String> configDS = env.socketTextStream("", 2222);

        //1.将配置流广播出去
        MapStateDescriptor<String, Integer> broadcastMapState = new MapStateDescriptor<>("broadcast-state", Types.STRING, Types.INT);
        BroadcastStream<String> configBS = configDS.broadcast(broadcastMapState);

        //2.把数据流和广播后的配置流connect起来
        BroadcastConnectedStream<WaterSenor, String> dataBCS = dataSrouce.connect(configBS);

        //3.调用process
        dataBCS.process(
                new BroadcastProcessFunction<WaterSenor, String, String>() {
                    @Override
                    //这个处理的是数据流
                    public void processElement(WaterSenor value, ReadOnlyContext ctx, Collector<String> out) throws Exception {
                        //通过上下文获取广播状态，取出里面的值（只读，不能修改）
                        ReadOnlyBroadcastState<String, Integer> broadcastState = ctx.getBroadcastState(broadcastMapState);
                        //双流的话，数据有先后，数据流的数据如果比广播流的数据先过来，那没有广播的状态
                        Integer threshold = broadcastState.get("threshold");
                        threshold = threshold==null ? 0: threshold;
                        if (value.getVc()>threshold) {
                            out.collect(value+",水位超过指定的阈值，阈值是:"+threshold+"!!!");
                        }
                    }

                    @Override
                    //这个处理的是配置的广播流
                    public void processBroadcastElement(String value, Context ctx, Collector<String> out) throws Exception {
                        //通过上下文获取广播状态，往里面写数据
                        BroadcastState<String, Integer> broadcastState = ctx.getBroadcastState(broadcastMapState);
                        broadcastState.put("threshold",Integer.valueOf(value));
                    }
                }
        ).print();


        env.execute();
    }

}
