package basic.state;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/10 11:37
 * @description：  算子状态（也就是没有经过keyby的），整个算子共享一个状态，
 *          liststate&unionliststate的案例，计算map算子中数据的条数
 *     liststate和unionliststate的区别  在上游到下游的并行度改变的时候
 *     liststate是将所有并行度的状态收集起来，然后轮询发送给下游的子任务，而unionliststate是给下游子任务每个一份完整的。
 *
 */
public class OperatorListStateDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.socketTextStream("",1111)
                .map(new MyCountMapFunction())
                .print();
        env.execute();
    }

    //1.如果自定义算子状态，那么首先要实现CheckpointedFunction这个接口
    public static class MyCountMapFunction implements MapFunction<String,Long>, CheckpointedFunction {
        private long count=0L;
        private ListState<Long> state;
        @Override
        public Long map(String value) throws Exception {
            return ++count;
        }
        @Override
        //将算子状态做快照，将本地变量拷贝到算子状态中
        public void snapshotState(FunctionSnapshotContext context) throws Exception {
            System.out.println("snapshotState...");
            //清空算子状态，并且将本地变量添加到算子状态中
            state.clear();
            state.add(count);
        }

        @Override
        //初始化本地变量，从状态中，把数据添加到本地变量，每个子任务调用一次，一般是出错之后初始化用的
        public void initializeState(FunctionInitializationContext context) throws Exception {
            System.out.println("initializeState...");
            //从上下文获取算子状态
            state = context.getOperatorStateStore()
                    .getListState(new ListStateDescriptor<Long>("state", Types.LONG));
            //从算子状态中 把数据拷贝到本地变量
            if (context.isRestored()) {
                for (Long c : state.get()) {
                    count+=c;
                }
            }
        }
    }

}
