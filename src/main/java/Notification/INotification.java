package Notification;

public interface INotification
{
    /**
     * 进行通知
     * @param title 通知的题目
     * @param content 通知内容
     * @return 当且仅当通知成功，返回true
     */
    boolean notify(String title, final String content);
}
