package edu.kafkapractice.plugin.file.client;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import edu.kafkapractice.plugin.KafkaNotification;
import edu.kafkapractice.plugin.exception.KafkaValidationException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class KafkaClientManager {
    private KafkaClient kafkaClient;
    private volatile boolean connecting = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static KafkaClientManager getInstance(Project project) {
        return project.getService(KafkaClientManager.class);
    }

    public void connectToKafka(String bootstrapServers, Runnable onConnectionStart, Runnable onConnectionEnd) {
        if (kafkaClient == null) {
            connecting = true;
            ApplicationManager.getApplication().invokeLater(onConnectionStart);
            new Thread(() -> {
                try {
                    verifyClusterConnection(bootstrapServers);
                    kafkaClient = new KafkaClient(bootstrapServers);
                    ApplicationManager.getApplication().invokeLater(() ->
                            KafkaNotification.showNotification("Connected to Kafka successfully!", NotificationType.INFORMATION)
                    );
                } catch (Exception ex) {
                    ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showErrorDialog(String.format("Failed to connect to kafka: %s", bootstrapServers), "Error")
                    );
                } finally {
                    connecting = false;
                    ApplicationManager.getApplication().invokeLater(onConnectionEnd);
                }
            }).start();
        } else {
            Messages.showErrorDialog(String.format("There is an active KafkaProducer: %s", kafkaClient.getBootstrapServers()), "Error");
        }
    }

    public void sendMessage(String topic, String key, String message, String headersText, boolean createTopic, Runnable onSuccess) {
        if (kafkaClient == null) {
            Messages.showErrorDialog("KafkaProducer is not initialized", "Error");
            return;
        }

        executorService.submit(() -> {
            try {
                if (createTopic || doesTopicExist(topic)) {
                    Map<String, String> headers = parseHeaders(headersText.split("\\r?\\n"));
                    kafkaClient.sendMessage(topic, key, message, headers);
                    SwingUtilities.invokeLater(onSuccess);
                    SwingUtilities.invokeLater(() ->
                            KafkaNotification.showNotification(String.format("Published: %s", key), NotificationType.INFORMATION)
                    );
                } else {
                    SwingUtilities.invokeLater(() ->
                            Messages.showErrorDialog(String.format("Topic does not exist: %s", topic), "Error")
                    );
                }
            } catch (KafkaValidationException e) {
                SwingUtilities.invokeLater(() ->
                        Messages.showErrorDialog(e.getMessage(), "Error")
                );
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        Messages.showErrorDialog(String.format("Publish failed: %s", e), "Error")
                );
            }
        });
    }

    public boolean doesTopicExist(String topic) throws ExecutionException, InterruptedException {
        String bootstrapServers = kafkaClient.getBootstrapServers();
        try (AdminClient adminClient = createAdminClient(bootstrapServers, 3000, 10000)) {
            Set<String> topics = adminClient.listTopics().names().get();
            return topics.contains(topic);
        }
    }

    private Map<String, String> parseHeaders(String[] headersLines) {
        return Arrays.stream(headersLines)
                .filter(this::isHeaderValid)
                .map(line -> line.split(":"))
                .collect(Collectors.toMap(lines -> lines[0],
                        lines -> Arrays.stream(lines).skip(1).collect(Collectors.joining(":"))));
    }

    private boolean isHeaderValid(String line) {
        if (line.trim().isEmpty()) {
            return false;
        } else if (!line.contains(":")) {
            throw new KafkaValidationException(String.format("Invalid header format: %s. Each header must be in the format 'key:value'", line));
        }
        return true;
    }

    public void closeConnection() {
        if (kafkaClient != null) {
            kafkaClient.close();
            kafkaClient = null;
            KafkaNotification.showNotification("Kafka connection closed.", NotificationType.INFORMATION);
        }
    }

    public boolean isConnecting() {
        return connecting;
    }

    private AdminClient createAdminClient(String bootstrapServers, Integer requestTimeoutMs, Integer apiTimeoutMs) {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminProps.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, Integer.toString(requestTimeoutMs));
        adminProps.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, Integer.toString(apiTimeoutMs));
        return AdminClient.create(adminProps);
    }

    private void verifyClusterConnection(String bootstrapServers) throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = createAdminClient(bootstrapServers, 10000, 30000)) {
            adminClient.describeCluster().nodes().get();
        }
    }
}
