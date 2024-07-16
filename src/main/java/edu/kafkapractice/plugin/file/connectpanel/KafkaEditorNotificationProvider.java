package edu.kafkapractice.plugin.file.connectpanel;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.components.JBTextField;
import edu.kafkapractice.plugin.file.client.KafkaClientManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class KafkaEditorNotificationProvider implements EditorNotificationProvider {

    @Override
    @Nullable
    public Function<? super FileEditor, ? extends JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.getName().endsWith(".kafka")) {
            return editor -> {
                JPanel panel = new JPanel();

                GridBagLayout layout = new GridBagLayout();
                panel.setLayout(layout);
                GridBagConstraints gbc = new GridBagConstraints();

                gbc.gridx = 0;
                gbc.gridy = 0;
                JLabel bootstrapServersLabel = new JLabel("Bootstrap Servers:");
                panel.add(bootstrapServersLabel, gbc);

                gbc.gridx = 1;
                JBTextField bootstrapServersField = new JBTextField(20);
                panel.add(bootstrapServersField, gbc);

                gbc.gridx = 2;
                JButton connectButton = new JButton("Connect");
                connectButton.addActionListener(e -> {
                    try {
                        KafkaClientManager.connectToKafka(bootstrapServersField.getText());
                    } catch (TimeoutException ex) {
                        Messages.showErrorDialog("Failed to connect to Kafka: " + ex.getMessage(), "Error");
                    }
                });
                panel.add(connectButton, gbc);

                gbc.gridx = 3;
                JButton closeButton = new JButton("Close Connection");
                closeButton.addActionListener(e -> {
                    KafkaClientManager.closeConnection();
                });
                panel.add(closeButton, gbc);

                return panel;
            };
        }
        return null;
    }
}
