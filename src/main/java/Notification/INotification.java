package Notification;

import Context.Context;

public interface INotification
{
    /**
     * 进行通知
     *
     * @param context 上下文
     * @return 当且仅当通知成功，返回true
     */
    boolean notify(Context context);
}
