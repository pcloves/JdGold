package Monitor;

import Context.Context;

public interface IGoldMonitor
{
    String getName();

    /**
     * @param context 上下文
     * @return 返回null表示不需要预警，否则表示预警的内容
     */
    String monitor(final Context context);
}
