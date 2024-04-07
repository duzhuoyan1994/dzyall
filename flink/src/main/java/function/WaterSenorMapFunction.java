package function;

import bean.WaterSenor;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 15:32
 * @description：
 */
public class WaterSenorMapFunction implements MapFunction<String, WaterSenor> {
    @Override
    public WaterSenor map(String value) throws Exception {
        return new WaterSenor(value.split(" ")[0],
                Integer.parseInt(value.split(" ")[1]),
                Integer.parseInt(value.split(" ")[2]));
    }
}
