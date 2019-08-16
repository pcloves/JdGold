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
}
