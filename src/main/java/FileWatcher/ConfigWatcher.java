package FileWatcher;

import Bootstrap.Run;
import Monitor.EMonitor;
import Monitor.IGoldMonitor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConfigWatcher extends FileWatcher
{
    private final Run run;

    public ConfigWatcher(Run run) {
        this.run = run;
        this.setDaemon(true);
    }

    @Override
    public void doOnChange() {

        try
        {
            System.out.println("配置文件发生改变，重新加载监控规则");
            final byte[] bytes = Files.readAllBytes(getFile().toPath());

            String fileContent = new String(bytes, StandardCharsets.UTF_8);
            final JSONArray jsonArray = JSON.parseArray(fileContent);
            if (jsonArray == null) {
                return;
            }

            final int size = jsonArray.size();
            final Map<String, IGoldMonitor> monitorMap = new HashMap<>(size);

            for (int i = 0; i < size; i++)
            {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);

                final String monitorName = jsonObject.getString("name");
                final Boolean stop = jsonObject.getBoolean("stop");
                if (stop != null && stop) {
                    continue;
                }

                final EMonitor monitor = EMonitor.of(monitorName);

                if (monitor == EMonitor.Invalid) {
                    System.out.println("无效的监控规则：" + monitorName + "，跳过不予执行");
                    continue;
                }

                try
                {
                    final Constructor<? extends IGoldMonitor> constructor = monitor.clazz.getConstructor(JSONObject.class);
                    final IGoldMonitor monitorInstance = constructor.newInstance(jsonObject);

                    System.out.println(jsonObject.toString(SerializerFeature.PrettyFormat));
                    monitorMap.put(monitorName + i, monitorInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            run.setMonitorMap(monitorMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
