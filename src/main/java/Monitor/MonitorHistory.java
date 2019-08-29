package Monitor;

import Context.Context;
import Utils.Util;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 监控金价一段时间内达到最高、最低值
 */
public class MonitorHistory implements IGoldMonitor
{
    private static final String contentFormat = "金价达到%d分钟内最%s预警，当前值：%.2f";

    private final boolean upThreshold;
    private final int monitorIntervalMilliSecond;

    public MonitorHistory(JSONObject param)
    {
        this.upThreshold = param.getBoolean("upThreshold");
        this.monitorIntervalMilliSecond = param.getInteger("monitorIntervalMinutes") * 60 * 1000;
    }

    @Override
    public String getName() {
        return "金价历史阈值预警";
    }

    @Override
    public String monitor(Context context) {

        final JSONArray priceArray = context.get(Context.ContextType.PriceArray, new JSONArray());
        final JSONObject jsonPriceNew = priceArray.getJSONObject(0);

        final Float priceCurrent = Util.getPrice(jsonPriceNew);
        final Long timeCurrent = Util.getTime(jsonPriceNew);

        final float priceThreshold = priceArray.stream()
                .filter(s -> timeCurrent - Util.getTime((JSONObject) s) <= this.monitorIntervalMilliSecond)
                .map(s -> Util.getPrice((JSONObject) s))
                .reduce((s1, s2) -> upThreshold ? Math.max(s1, s2) : Math.min(s1, s2))
                .orElse(upThreshold ? Float.MAX_VALUE : Float.MIN_VALUE);

        if ((upThreshold && priceCurrent >= priceThreshold) || (!upThreshold && priceCurrent <= priceThreshold)) {

            final String s = upThreshold ? "高" : "低";
            return String.format(contentFormat, monitorIntervalMilliSecond / 60 / 1000, s, priceCurrent);
        }

        return null;
    }
}
