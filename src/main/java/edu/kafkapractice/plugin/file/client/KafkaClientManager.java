package edu.kafkapractice.plugin.file.client;

import com.intellij.notification.NotificationType;
import edu.kafkapractice.plugin.KafkaNotification;

import java.util.concurrent.TimeoutException;

public class KafkaClientManager {
    private static KafkaClient kafkaClient;

    public static void connectToKafka(String bootstrapServers) throws TimeoutException {
        if (kafkaClient == null) {
            kafkaClient = new KafkaClient(bootstrapServers);
            KafkaNotification.showNotification("Connected to Kafka successfully!", NotificationType.INFORMATION);
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
}
