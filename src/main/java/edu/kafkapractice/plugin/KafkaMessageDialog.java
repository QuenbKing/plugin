package edu.kafkapractice.plugin;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import edu.kafkapractice.plugin.file.client.KafkaClient;
import edu.kafkapractice.plugin.file.client.KafkaClientManager;
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

        KafkaClient kafkaClient = KafkaClientManager.getInstance();
        if (kafkaClient != null) {
            kafkaClient.sendMessage(topic, key, message, headers);
            super.doOKAction();
            SwingUtilities.invokeLater(() -> {
                KafkaNotification.showNotification(String.format("Published: %s", key), NotificationType.INFORMATION);
            });
        } else {
            Messages.showErrorDialog("KafkaProducer is not initialized", "Error");
        }
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        initFields();
        addFieldsToPanel(panel, gbc);

        loadMessageDialogData();

        return panel;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void initFields() {
        topicField = new JBTextField();
        keyField = new JBTextField();
        messageArea = new JTextArea(5, 20);
        headersArea = new JTextArea(5, 20);
    }

    private void addFieldsToPanel(JPanel panel, GridBagConstraints gbc) {
        addLabelAndField(panel, gbc, "Topic:", topicField, 0);
        addLabelAndField(panel, gbc, "Key:", keyField, 2);
        addLabelAndTextArea(panel, gbc, "Message:", messageArea, 1);
        addLabelAndTextArea(panel, gbc, "Headers:", headersArea, 2);
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField, int gridx) {
        gbc.gridx = gridx;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = gridx + 1;
        gbc.gridwidth = 1;
        panel.add(textField, gbc);
    }

    private void addLabelAndTextArea(JPanel panel, GridBagConstraints gbc, String labelText, JTextArea textArea, int gridy) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(createLabeledArea(labelText, textArea), gbc);
    }

    private JPanel createLabeledArea(String label, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(new JBScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private void setSizeRelativeToScreen(double widthFactor, double heightFactor) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * widthFactor);
        int height = (int) (screenSize.height * heightFactor);
        setSize(width, height);
    }

    private void loadMessageDialogData() {
        Document document = editor.getDocument();
        int startOffset = document.getLineStartOffset(lineNumber);
        String documentData = document.getText(new TextRange(startOffset, document.getTextLength()));
        String[] documentLines = documentData.split("\\r?\\n");
        parseLines(documentLines);
    }

    private void parseLines(String[] documentLines) {
        String topic = "";
        String key = "";
        StringBuilder messageBuilder = new StringBuilder();
        StringBuilder headersBuilder = new StringBuilder();

        for (int currentIndex = 1; currentIndex < documentLines.length; currentIndex++) {
            String line = documentLines[currentIndex];

            if (line.startsWith("###Send Message")) {
                break;
            } else if (line.startsWith("Topic:")) {
                topic = line.substring(6).trim();
            } else if (line.startsWith("Key:")) {
                key = line.substring(4).trim();
            } else if (line.startsWith("Headers:")) {
                currentIndex++;
                currentIndex = appendSectionContent(documentLines, currentIndex, headersBuilder);
            } else if (line.startsWith("Message:")) {
                currentIndex++;
                currentIndex = appendSectionContent(documentLines, currentIndex, messageBuilder);
            }
        }

        topicField.setText(topic);
        keyField.setText(key);
        messageArea.setText(messageBuilder.toString().trim());
        headersArea.setText(headersBuilder.toString().trim());
    }

    private int appendSectionContent(String[] lines, int startIndex, StringBuilder builder) {
        while (startIndex < lines.length && isNotStartOfNewSection(lines[startIndex])) {
            builder.append(lines[startIndex]).append("\n");
            startIndex++;
        }
        return startIndex - 1;
    }

    private boolean isNotStartOfNewSection(String line) {
        return !line.startsWith("Topic:") && !line.startsWith("Key:")
                && !line.startsWith("Headers:") && !line.startsWith("Message:")
                && !line.startsWith("###Send Message");
    }
}