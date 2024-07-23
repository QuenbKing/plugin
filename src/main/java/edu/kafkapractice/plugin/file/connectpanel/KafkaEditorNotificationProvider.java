package edu.kafkapractice.plugin.file.connectpanel;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.components.JBTextField;
import edu.kafkapractice.plugin.file.client.KafkaClientManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class KafkaEditorNotificationProvider implements EditorNotificationProvider {

    private static KafkaEditorNotificationProvider instance;

    private final Map<VirtualFile, JButton> connectButtons = new HashMap<>();

    public KafkaEditorNotificationProvider() {
        instance = this;
    }

    @Override
    @Nullable
    public Function<? super FileEditor, ? extends JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.getName().endsWith(".kafka")) {
            return editor -> createKafkaPanel(file);
        }
        return null;
    }

    private JPanel createKafkaPanel(VirtualFile file) {
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
            KafkaClientManager.connectToKafka(bootstrapServersField.getText(),
                    this::disableAllConnectButtons,
                    this::enableAllConnectButtons);
        });
        panel.add(connectButton, gbc);
        registerConnectButton(connectButton, file);

        gbc.gridx = 3;
        JButton closeButton = new JButton("Close Connection");
        closeButton.addActionListener(e -> {
            KafkaClientManager.closeConnection();
        });
        panel.add(closeButton, gbc);

        return panel;
    }

    public static KafkaEditorNotificationProvider getInstance() {
        return instance;
    }

    public void removeConnectButton(VirtualFile file) {
        connectButtons.remove(file);
    }

    private void disableAllConnectButtons() {
        for (JButton button : connectButtons.values()) {
            button.setEnabled(false);
        }
    }

    private void enableAllConnectButtons() {
        for (JButton button : connectButtons.values()) {
            button.setEnabled(true);
        }
    }

    private void registerConnectButton(JButton button, VirtualFile file) {
        connectButtons.put(file, button);
        button.setEnabled(!KafkaClientManager.isConnecting());
    }
}
