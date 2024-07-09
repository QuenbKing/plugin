package edu.kafkapractice.plugin;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class KafkaMessageDialog extends DialogWrapper {

    private final Editor editor;
    private final int lineNumber;
    private JBTextField topicField;
    private JBTextField keyField;
    private JTextArea messageArea;
    private JTextArea headersArea;

    public KafkaMessageDialog(Editor editor, int lineNumber) {
        super(true);
        this.editor = editor;
        this.lineNumber = lineNumber;
        setTitle("Send Message to Kafka");
        init();
        setResizable(true);
        setSizeRelativeToScreen(0.4, 0.4);
    }

    private void setSizeRelativeToScreen(double widthFactor, double heightFactor) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * widthFactor);
        int height = (int) (screenSize.height * heightFactor);
        setSize(width, height);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        topicField = new JBTextField();
        keyField = new JBTextField();
        messageArea = new JTextArea(5, 20);
        headersArea = new JTextArea(5, 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Topic:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(topicField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Key:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(keyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(createLabeledArea("Message", messageArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(createLabeledArea("Headers", headersArea), gbc);

        loadKafkaData();

        return panel;
    }

    private JPanel createLabeledArea(String label, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(new JBScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private void loadKafkaData() {
        String content = editor.getDocument().getText();
        String[] lines = content.split("\\r?\\n");

        StringBuilder sectionBuilder = new StringBuilder();
        for (int i = lineNumber + 1; i < lines.length; i++) {
            if (lines[i].startsWith("###Send Message")) {
                break;
            }
            sectionBuilder.append(lines[i]).append("\n");
        }

        String section = sectionBuilder.toString().trim();
        parseSection(section);
    }

    private void parseSection(String section) {
        String[] lines = section.split("\\r?\\n");

        String topic = "";
        String key = "";
        String message = "";
        Map<String, String> headers = new HashMap<>();

        boolean parsingHeaders = false;
        for (String line : lines) {
            if (line.startsWith("Topic:")) {
                topic = line.substring(6).trim();
                parsingHeaders = false;
            } else if (line.startsWith("Key:")) {
                key = line.substring(4).trim();
                parsingHeaders = false;
            } else if (line.startsWith("Message:")) {
                message = line.substring(8).trim();
                parsingHeaders = false;
            } else if (line.startsWith("Headers:")) {
                parsingHeaders = true;
            } else if (parsingHeaders) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    headers.put(parts[0].trim(), parts[1].trim());
                }
            }
        }

        topicField.setText(topic);
        keyField.setText(key);
        messageArea.setText(message);

        StringBuilder headersBuilder = new StringBuilder();
        headers.forEach((headerKey, headerValue) -> headersBuilder.append(headerKey).append(":").append(headerValue).append("\n"));
        headersArea.setText(headersBuilder.toString().trim());
    }

    @Override
    protected void doOKAction() {
        String topic = topicField.getText();
        String key = keyField.getText();
        String message = messageArea.getText();
        String[] headersLines = headersArea.getText().split("\\r?\\n");

        Map<String, String> headers = new HashMap<>();
        for (String headerLine : headersLines) {
            String[] headerParts = headerLine.split(":");
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        KafkaClient kafkaClient = new KafkaClient("localhost:29092");
        kafkaClient.sendMessage(topic, key, message, headers);
        kafkaClient.close();

        super.doOKAction();
    }
}