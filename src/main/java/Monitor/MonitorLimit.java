package Monitor;

import Context.Context;
import com.alibaba.fastjson.JSONObject;

/**
 * 监控金价高于或低于设定的阈值
 */
public class MonitorLimit implements IGoldMonitor
{
    private static final String contentFormat = "金价自定义%s限预警！设定值:%.2f，当前：%.2f";
    private final boolean upThreshold;
    private final float priceThreshold;

    public MonitorLimit(final JSONObject param) {
        this.upThreshold = param.getBoolean("upThreshold");
        this.priceThreshold = param.getFloat("priceThreshold");
    }

    @Override
    public String getName() {
        return "金价自定义阈值预警";
    }

    @Override
    public String monitor(Context context) {

        final Float priceCurrent = context.get(Context.ContextType.NewestPrice, 0.0f);
        if ((upThreshold && priceCurrent >= priceThreshold) || (!upThreshold && priceCurrent <= priceThreshold)) {

            final String s = upThreshold ? "上" : "下";
            return String.format(contentFormat, s, priceThreshold, priceCurrent);
        }

        return null;
    }
}
