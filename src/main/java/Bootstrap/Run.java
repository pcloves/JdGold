package Bootstrap;

import Context.Context;
import FileWatcher.ConfigWatcher;
import FileWatcher.FileWatcher;
import Monitor.IGoldMonitor;
import Notification.INotification;
import Notification.NotificationWechat;
import Utils.Config;
import Utils.Util;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Run implements Runnable
{
    private final Context context = new Context();
    private final INotification notification = new NotificationWechat();
    private volatile Map<String, IGoldMonitor> monitorMap = new HashMap<>(1);
    private final FileWatcher fileWatcher = new ConfigWatcher(this);

    public void setMonitorMap(Map<String, IGoldMonitor> monitorMap) {
        this.monitorMap = monitorMap;
    }

    private Run() {
        final Path path = Paths.get(Config.MonitorConfig);
        if (!Files.exists(path))
        {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println("创建监控配置文件：" + Config.MonitorConfig + "失败，请使用管理员权限运行！");
                System.exit(-1);
            }
        }

        fileWatcher.setFile(path.toFile());
        fileWatcher.start();
        fileWatcher.doOnChange();
    }

    public void run()
    {
        try
        {
            if (!updatePrice()) {
                return;
            }

            final JSONArray priceArray = context.get(Context.ContextType.PriceArray, new JSONArray());

            final JSONObject jsonPrice1 = priceArray.getJSONObject(0);
            final Float price1 = Util.getPrice(jsonPrice1);
            final long time1 = Util.getTime(jsonPrice1);

            final JSONObject jsonPrice2 = priceArray.getJSONObject(1);
            final Float price2 = Util.getPrice(jsonPrice2);
            final long time2 = Util.getTime(jsonPrice2);

            final String date = Config.DateFormat.format(new Date(time1));
            final float priceChange = price1 - price2;

            System.out.println("最新金价时间：" + date + "，最新金价：" + price1 + "，变化值：" + priceChange + "，间隔秒数：" + (time1 - time2) / 1000.0f);

            final StringBuilder builderTitle = new StringBuilder(128);
            final StringBuilder builderContent = new StringBuilder(128);
            builderContent.append(date).append(":");

            int needNotify = 0;

            final Collection<IGoldMonitor> values = monitorMap.values();
            for (IGoldMonitor monitor : values)
            {
                final String content = monitor.monitor(context);
                if (content != null) {

                    builderTitle.append(monitor.getName()).append(" ");
                    builderContent.append(content).append(" ");

                    needNotify++;
                }
            }

            if (needNotify > 0)
            {
                final String title = builderTitle.toString();
                final String content = builderContent.toString();

                context.add(Context.ContextType.NotifyTitle, title);
                context.add(Context.ContextType.NotifyContent, content);

                if (notification.notify(context))
                {
                    System.out.println("条件满足，开始预警：");
                    System.out.println("    标题：" + title);
                    System.out.println("    内容：" + content);
                }
            }

            System.out.println("=====================================================================");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新价格数组，数组内的元素形如：
     * {"name":"2019-08-29 00:24:00","value":["2019-08-29 00:24:00","356.73"]}
     */
    private boolean updatePrice()
    {
        final JSONArray jsonPriceArray = context.get(Context.ContextType.PriceArray, new JSONArray());
        if (jsonPriceArray.isEmpty())
        {
            System.out.println("当天价格为空，今天是星期天？");
            //初始化获取当天的价格
            final JSONArray priceToday = Util.getTodayPrice();
            context.add(Context.ContextType.PriceArray, priceToday);
            return false;
        }

        final JSONObject jsonPriceNewCache = jsonPriceArray.getJSONObject(0);
        final JSONObject jsonPriceNew = Util.getNewestPrice();

        final long timeNewInCache = Util.getTime(jsonPriceNewCache);
        final long timeNew = jsonPriceNew.getLong("time");

        if (timeNew <= timeNewInCache) {
            return false;
        }

        final JSONObject priceAdd = new JSONObject();
        final String time = Config.DateFormat.format(new Date(timeNew));

        final JSONArray jsonValueArray = new JSONArray();
        jsonValueArray.add(time);
        jsonValueArray.add(jsonPriceNew.getFloat("price"));

        priceAdd.put("name", time);
        priceAdd.put("value", jsonValueArray);

        jsonPriceArray.add(0, priceAdd);

        if (jsonPriceArray.size() > Config.PriceArrayMaxCacheSize) {
            jsonPriceArray.remove(jsonPriceArray.size() - 1);
        }

        return true;
    }

    public static void main(String[] args)
    {

        final String secretKey = System.getProperty(Config.SecretKey, "");
        if (secretKey.equals("")) {
            System.out.println("请指定-DsecretKey参数，该参数请到：http://sc.ftqq.com 绑定微信推送并获取");
            System.exit(-1);
        }

        System.setProperty(Config.SecretKey, secretKey);

        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        final Run run = new Run();
        executorService.scheduleAtFixedRate(run, 0, Config.ThreadTickSecond, TimeUnit.SECONDS);
    }
}
