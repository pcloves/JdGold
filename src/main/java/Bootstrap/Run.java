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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
        try {

            final JSONArray priceToday = Util.getTodayPrice();
            final JSONObject newestData = Util.getNewestData();
            final float newestPrice = newestData.getFloat("price");
            final long newestTime = newestData.getLong("time");

            context.add(Context.ContextType.PriceToday, priceToday);
            context.add(Context.ContextType.NewestTime, newestTime);
            final Float priceLast = context.add(Context.ContextType.NewestPrice, newestPrice);

            final String date = Config.DateFormat.format(new Date(newestTime));
            final float priceChange = newestPrice - (priceLast == null ? 0 : priceLast);

            System.out.println("最新金价时间：" + date + "，最新金价：" + newestPrice + "，变化值：" + priceChange);
            final Iterator<IGoldMonitor> iterator = monitorMap.values().iterator();

            StringBuilder builderTitle = new StringBuilder(128);
            StringBuilder builderContent = new StringBuilder(128);
            builderContent.append(date).append(":");

            int needNotify = 0;
            while (iterator.hasNext())
            {
                final IGoldMonitor monitor =  iterator.next();
                final String content = monitor.monitor(context);
                if (content != null) {

                    builderTitle.append(monitor.getName()).append(" ");
                    builderContent.append(content).append(" ");

                    needNotify++;
                }
            }

            if (needNotify > 0) {
                final String title = builderTitle.toString();
                final String content = builderContent.toString();
                if (notification.notify(title, content))
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
        executorService.scheduleAtFixedRate(run, 0, 15, TimeUnit.SECONDS);
    }
}
