package Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

public class Util
{
    /**
     *和获取最新的金价
     *
     * json格式：
     * {
     * 	"resultCode": 0,
     * 	"resultMsg": "操作成功",
     * 	"resultData": {
     * 		"datas": {
     * 			"productSku": "P005",
     * 			"demode": false,
     * 			"priceNum": "8B2AE5F3F7DDD73555B62B590E96233A",
     * 			"price": "347.17",
     * 			"id": 5503879,
     * 			"time": "1565864548000"
     *                },
     * 		"status": "SUCCESS"* 	},
     * 	"channelEncrypt": 0
     * }
     *
     * @return 最新价格
     */
    public static JSONObject getNewestData()
    {
        try {

            final CloseableHttpClient httpClient = HttpClients.createDefault();
            final HttpGet httpGet = new HttpGet(Config.URLLastPrice);
            final CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                final String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                final JSONObject json = JSON.parseObject(content);

                return json.getJSONObject("resultData").getJSONObject("datas");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        return null;
    }


    /**
     *
     * @return 获取当天的价格
     */
    public static JSONArray getTodayPrice()
    {
        try {

            final CloseableHttpClient httpClient = HttpClients.createDefault();
            final HttpGet httpGet = new HttpGet(Config.URLTodayPrices);
            final CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {

                final String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                final JSONObject json = JSON.parseObject(content);
                final JSONArray jsonArray = json.getJSONObject("resultData").getJSONArray("datas");

                //反向排序
                Collections.reverse(jsonArray);

                return jsonArray;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取当前时间的前<code>seconds</code>秒时的数据
     * @param seconds 最近的秒数
     * @return 如果获取结束，则返回最新的价格
     */
    public static JSONObject getLastSecondData(JSONArray jsonArray, int seconds)
    {
        final JSONObject current = jsonArray.getJSONObject(0);
        final long timeCurrent = getTime(current);

        return jsonArray
                .stream()
                .limit(seconds / 30 + 10)
                .map(s -> (JSONObject)s)
                .filter(s -> getTime(s) + (seconds * 1000) <= timeCurrent)
                .findFirst().orElse(current);
    }

    public static float getPrice(final JSONObject jsonObject)
    {
        final JSONArray value = jsonObject.getJSONArray("value");
        return value.getFloat(1);
    }

    public static long getTime(final JSONObject jsonObject)
    {
        final JSONArray value = jsonObject.getJSONArray("value");
        final String timeString = value.getString(0);

        long time = -1;
        try {
            final Date date = Config.DateFormat.parse(timeString);
            time = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }
}
