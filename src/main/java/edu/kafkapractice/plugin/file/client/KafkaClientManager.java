package edu.kafkapractice.plugin.file.client;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import edu.kafkapractice.plugin.KafkaNotification;

import java.util.concurrent.TimeoutException;

public class KafkaClientManager {
    private static KafkaClient kafkaClient;
    private static volatile boolean connecting = false;

    public static void connectToKafka(String bootstrapServers, Runnable onConnectionStart, Runnable onConnectionEnd) {
        if (kafkaClient == null) {
            connecting = true;
            ApplicationManager.getApplication().invokeLater(onConnectionStart);
            new Thread(() -> {
                try {
                    kafkaClient = new KafkaClient(bootstrapServers);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        KafkaNotification.showNotification("Connected to Kafka successfully!", NotificationType.INFORMATION);
                    });
                } catch (TimeoutException ex) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showErrorDialog("Failed to connect to Kafka: " + ex.getMessage(), "Error");
                    });
                } finally {
                    connecting = false;
                    ApplicationManager.getApplication().invokeLater(onConnectionEnd);
                }
            }).start();
        }
    }

    public static KafkaClient getInstance() {
        return kafkaClient;
    }

    public static void closeConnection() {
        if (kafkaClient != null) {
            kafkaClient.close();
            kafkaClient = null;
            KafkaNotification.showNotification("Kafka connection closed.", NotificationType.INFORMATION);
        }
    }

    public static boolean isConnecting() {
        return connecting;
    }
}
