package edu.kafkapractice.plugin.file.client;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import edu.kafkapractice.plugin.KafkaNotification;
import edu.kafkapractice.plugin.exception.KafkaValidationException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import javax.swing.*;
import java.time.Duration;
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
    private AdminClient adminClient;
    private DialogWrapper currentDialog;
    private ExecutorService executorService;
    private volatile boolean connecting = false;
    private volatile boolean isSending = false;

    public static KafkaClientManager getInstance(Project project) {
        return project.getService(KafkaClientManager.class);
    }

    public void connectToKafka(String bootstrapServers, Runnable onConnectionStart, Runnable onConnectionEnd) {
        if (kafkaClient == null) {
            executorService = Executors.newSingleThreadExecutor();
            connecting = true;
            ApplicationManager.getApplication().invokeLater(onConnectionStart);
            executorService.submit(() -> {
                try {
                    adminClient = createAdminClient(bootstrapServers, 10000, 30000);
                    verifyClusterConnection(adminClient);
                    kafkaClient = new KafkaClient(bootstrapServers);
                    showNotification("Connected to Kafka successfully!");
                } catch (Exception ex) {
                    showError(String.format("Failed to connect to kafka: %s", bootstrapServers));
                    closeAdminClient();
                    shutdownExecutorService();
                } finally {
                    connecting = false;
                    ApplicationManager.getApplication().invokeLater(onConnectionEnd);
                }
            });
        } else {
            showError(String.format("There is an active KafkaProducer: %s", kafkaClient.getBootstrapServers()));
        }
    }

    public String getKafkaConnectionInfo() {
        if (kafkaClient != null) {
            return String.format("Connected to Kafka at %s", kafkaClient.getBootstrapServers());
        } else {
            return "Not connected to Kafka";
        }
    }

    public boolean isConnecting() {
        return connecting;
    }

    public void setCurrentDialog(DialogWrapper dialog) {
        this.currentDialog = dialog;
        updateDialogState();
    }

    public void clearCurrentDialog() {
        this.currentDialog = null;
    }

    public void sendMessage(String topic, String key, String message, String headersText, boolean createTopic, Runnable onSuccess) {
        if (kafkaClient == null) {
            showError("KafkaProducer is not initialized");
            return;
        }

        isSending = true;
        updateDialogState();

        executorService.submit(() -> {
            try {
                verifyClusterConnection(adminClient);
                if (createTopic || doesTopicExist(topic)) {
                    Map<String, String> headers = parseHeaders(headersText.split("\\r?\\n"));
                    kafkaClient.sendMessage(topic, key, message, headers);
                    SwingUtilities.invokeLater(onSuccess);
                    showNotification(String.format("Published: %s", key));
                } else {
                    showError(String.format("Topic does not exist: %s", topic));
                }
            } catch (KafkaValidationException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                showError(String.format("Publish failed: %s", e));
            } finally {
                isSending = false;
                updateDialogState();
            }
        });
    }

    public boolean isSendingMessage() {
        return isSending;
    }

    public void closeConnection() {
        shutdownExecutorService();
        closeAdminClient();
        closeProducer();
    }

    private AdminClient createAdminClient(String bootstrapServers, Integer requestTimeoutMs, Integer apiTimeoutMs) {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminProps.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, Integer.toString(requestTimeoutMs));
        adminProps.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, Integer.toString(apiTimeoutMs));
        return AdminClient.create(adminProps);
    }

    private void verifyClusterConnection(AdminClient adminClient) throws ExecutionException, InterruptedException {
        adminClient.describeCluster().nodes().get();
    }

    private boolean doesTopicExist(String topic) throws ExecutionException, InterruptedException {
        Set<String> topics = adminClient.listTopics().names().get();
        return topics.contains(topic);
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

    private void updateDialogState() {
        SwingUtilities.invokeLater(() -> {
            if (currentDialog != null) {
                currentDialog.setOKActionEnabled(!isSending);
            }
        });
    }

    private ModalityState getCurrentModalityState() {
        if (currentDialog != null && currentDialog.getContentPane().isShowing()) {
            return ModalityState.stateForComponent(currentDialog.getContentPane());
        } else {
            return ModalityState.defaultModalityState();
        }
    }

    private void closeProducer() {
        if (kafkaClient != null) {
            kafkaClient.close(Duration.ZERO);
            kafkaClient = null;
            showNotification("Kafka connection closed.");
        }
    }

    private void closeAdminClient() {
        if (adminClient != null) {
            adminClient.close(Duration.ZERO);
            adminClient = null;
        }
    }

    private void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    private void showError(String message) {
        ApplicationManager.getApplication().invokeLater(() ->
                        Messages.showErrorDialog(message, "Error"),
                getCurrentModalityState()
        );
    }

    private void showNotification(String message) {
        ApplicationManager.getApplication().invokeLater(() ->
                        KafkaNotification.showNotification(message, NotificationType.INFORMATION),
                getCurrentModalityState()
        );
    }
}