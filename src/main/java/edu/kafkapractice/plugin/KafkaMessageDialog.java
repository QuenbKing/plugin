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
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class KafkaMessageDialog extends DialogWrapper {

    private final Editor editor;
    private final int lineNumber;
    private JBTextField topicField;
    private JBTextField keyField;
    private JTextArea messageArea;
    private JTextArea headersArea;
    private JCheckBox createTopicCheckBox;

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
        KafkaClient kafkaClient = KafkaClientManager.getInstance();

        if (kafkaClient == null) {
            Messages.showErrorDialog("KafkaProducer is not initialized", "Error");
            return;
        }

        String topic = topicField.getText();

        if (createTopicCheckBox.isSelected() || kafkaClient.doesTopicExist(topic)) {
            String key = keyField.getText();
            String message = messageArea.getText();
            String[] headersLines = headersArea.getText().split("\\r?\\n");

            Map<String, String> headers = Arrays.stream(headersLines)
                    .map(headerLine -> headerLine.split(":"))
                    .filter(headerParts -> headerParts.length == 2)
                    .collect(Collectors.toMap(
                            headerParts -> headerParts[0].trim(),
                            headerParts -> headerParts[1].trim()
                    ));
            kafkaClient.sendMessage(topic, key, message, headers);
            super.doOKAction();
            SwingUtilities.invokeLater(() -> {
                KafkaNotification.showNotification(String.format("Published: %s", key), NotificationType.INFORMATION);
            });
        } else {
            Messages.showErrorDialog("Topic does not exist", "Error");
        }
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        initFields();
        addFieldsToPanel(panel, gbc);

        loadMessageDialogData();

        return panel;
    }

    private void initFields() {
        topicField = new JBTextField();
        keyField = new JBTextField();
        messageArea = new JTextArea(5, 20);
        headersArea = new JTextArea(5, 20);
        createTopicCheckBox = new JCheckBox("Create Topic");
    }

    private void addFieldsToPanel(JPanel panel, GridBagConstraints gbc) {
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createLabeledField("Topic:", topicField), gbc);

        gbc.gridx = 1;
        panel.add(createLabeledField("Key:", keyField), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(createLabeledArea("Message:", messageArea), gbc);

        gbc.gridy = 2;
        panel.add(createLabeledArea("Headers:", headersArea), gbc);

        gbc.gridy = 3;
        panel.add(createTopicCheckBox, gbc);
    }

    private JPanel createLabeledField(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLabeledArea(String labelText, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
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