package basic.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/4 22:23
 * @description： flink使用文件数据源，从文件读取数据
 */
public class CusFileSource {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //为了测试，统一将并行度调整为1
        env.setParallelism(1);
        FileSource<String> fileSource = FileSource.
                forRecordStreamFormat(new TextLineInputFormat(), new Path("E:\\flink_test\\aaa.txt")).build();
        env.fromSource(fileSource, WatermarkStrategy.noWatermarks(),"file-source").print();
        env.execute();

    }

}
