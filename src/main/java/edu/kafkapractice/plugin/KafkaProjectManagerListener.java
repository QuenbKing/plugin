package edu.kafkapractice.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import edu.kafkapractice.plugin.file.connectpanel.KafkaEditorNotificationProvider;
import org.jetbrains.annotations.NotNull;

public class KafkaProjectManagerListener implements ProjectManagerListener {
    @Override
    public void projectClosed(@NotNull Project project) {
        KafkaEditorNotificationProvider kafkaEditorNotificationProvider = KafkaEditorNotificationProvider.getInstance(project);
        if (kafkaEditorNotificationProvider != null) {
            KafkaEditorNotificationProvider.removeInstance(project);
        }
    }
}
