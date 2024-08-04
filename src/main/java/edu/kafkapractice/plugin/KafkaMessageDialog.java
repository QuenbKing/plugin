package edu.kafkapractice.plugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import edu.kafkapractice.plugin.file.client.KafkaClientManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class KafkaMessageDialog extends DialogWrapper {

    private final Editor editor;
    private final KafkaClientManager kafkaClientManager;
    private final int lineNumber;
    private JBTextField topicField;
    private JBTextField keyField;
    private JTextArea messageArea;
    private JTextArea headersArea;
    private JCheckBox createTopicCheckBox;
    private JLabel kafkaConnectionLabel;

    public KafkaMessageDialog(Editor editor, int lineNumber, KafkaClientManager kafkaClientManager) {
        super(true);
        this.editor = editor;
        this.lineNumber = lineNumber;
        this.kafkaClientManager = kafkaClientManager;
        setTitle("Send Message to Kafka");
        init();
        setResizable(true);
        updateOkButtonState();
    }

    @Override
    protected void doOKAction() {
        String topic = topicField.getText();
        String key = keyField.getText();
        String message = messageArea.getText();
        String headersText = headersArea.getText();
        boolean createTopic = createTopicCheckBox.isSelected();
        kafkaClientManager.sendMessage(topic, key, message, headersText, createTopic, this::closeDialog);
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

    @Override
    protected void dispose() {
        super.dispose();
        kafkaClientManager.clearCurrentDialog();
    }

    private void closeDialog() {
        close(DialogWrapper.OK_EXIT_CODE);
    }

    private void initFields() {
        kafkaConnectionLabel = new JLabel(kafkaClientManager.getKafkaConnectionInfo());
        kafkaConnectionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        kafkaConnectionLabel.setForeground(JBColor.BLUE);

        topicField = new JBTextField();
        topicField.setPreferredSize(new Dimension(300, 30));

        keyField = new JBTextField();
        keyField.setPreferredSize(new Dimension(300, 30));

        messageArea = new JTextArea(5, 90);
        messageArea.setLineWrap(true);

        headersArea = new JTextArea(5, 90);
        headersArea.setLineWrap(true);

        createTopicCheckBox = new JCheckBox("Create Topic");
    }

    private void addFieldsToPanel(JPanel panel, GridBagConstraints gbc) {
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(kafkaConnectionLabel, gbc);

        gbc.gridy = 1;
        panel.add(createLabeledField("Topic:", topicField), gbc);

        gbc.gridx = 1;
        panel.add(createLabeledField("Key:", keyField), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(createLabeledArea("Message:", messageArea), gbc);

        gbc.gridy = 3;
        panel.add(createLabeledArea("Headers:", headersArea), gbc);

        gbc.gridy = 4;
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

    private void updateOkButtonState() {
        setOKActionEnabled(!kafkaClientManager.isSendingMessage());
        kafkaClientManager.setCurrentDialog(this);
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