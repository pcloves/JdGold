package Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Config
{
    public static final DateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String MonitorConfig = "monitor.json";
    static final String URLLastPrice = "https://ms.jr.jd.com/gw/generic/hj/h5/m/latestPrice";
    static final String URLTodayPrices = "https://ms.jr.jd.com/gw/generic/hj/h5/m/todayPrices";
    public static final String SecretKey = "secretKey";
    public static final int ThreadTickSecond = 1;
    /**
     * 从<code>URLTodayPrices</code>获取价格，一天最多获取的数量
     */
    public static final int PriceArrayCacheSizePerDay = 24 * 60 / 2;
    /**
     * 后续每隔<code>ThreadTickSecond</>秒获取价格，1天最多获取的价格数
     */
    public static final int PriceArrayCacheAddSizePerDay = 24 * 60 * 60 / ThreadTickSecond;
    /**
     * 黄金价格最大缓存数量
     */
    public static final int PriceArrayMaxCacheSize = PriceArrayCacheSizePerDay + PriceArrayCacheAddSizePerDay * 2;
}
