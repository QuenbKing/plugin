package edu.kafkapractice.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

public class KafkaNotification {
    public static void showNotification(String message, NotificationType notificationType) {
        Notification notification = new Notification("Kafka Notifications", "Kafka plugin", message, notificationType);
        Notifications.Bus.notify(notification);
    }
}
