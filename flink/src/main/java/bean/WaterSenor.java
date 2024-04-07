package bean;

/**
 * @author ：duzhuoyan
 * @date ：Created in 2024/4/7 15:36
 * @description：  测试过程中用到的一个bean类
 */
public class WaterSenor {

    private String id;
    private int ts;
    private int vc;

    public WaterSenor(String id,int ts,int vc){
        this.id = id;
        this.ts = ts;
        this.vc = vc;
    }

    @Override
    public String toString() {
        return "WaterSenor{" +
                "id='" + id + '\'' +
                ", ts=" + ts +
                ", vc=" + vc +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getVc() {
        return vc;
    }

    public void setVc(int vc) {
        this.vc = vc;
    }
}
