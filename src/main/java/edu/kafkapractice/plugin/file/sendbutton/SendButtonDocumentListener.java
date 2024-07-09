package edu.kafkapractice.plugin.file.sendbutton;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.*;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SendButtonDocumentListener implements DocumentListener {

    private static final String SEND_MESSAGE_MARKER = "###Send Message";

    private final Editor editor;
    private final Map<Integer, RangeHighlighter> sendButtonHighlighters = new HashMap<>();

    public SendButtonDocumentListener(Editor editor) {
        this.editor = editor;
    }

    @Override
    public void documentChanged(DocumentEvent e) {
        updateSendButtons();
    }

    private void updateSendButtons() {
        String content = editor.getDocument().getText();
        String[] lines = content.split("\\r?\\n");

        removeAllSendButtons();
        addSendButtons(lines);
    }

    public void addSendButtons(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(SEND_MESSAGE_MARKER) && !sendButtonHighlighters.containsKey(i)) {
                installSendButton(i);
            }
        }
    }

    private void removeAllSendButtons() {
        for (RangeHighlighter highlighter : sendButtonHighlighters.values()) {
            editor.getMarkupModel().removeHighlighter(highlighter);
        }
        sendButtonHighlighters.clear();
    }

    private void installSendButton(int lineNumber) {
        if (!sendButtonHighlighters.containsKey(lineNumber)) {
            KafkaSendButton button = new KafkaSendButton(editor, lineNumber);
            int startOffset = editor.getDocument().getLineStartOffset(lineNumber);
            int endOffset = editor.getDocument().getLineEndOffset(lineNumber);

            RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                    startOffset,
                    endOffset,
                    HighlighterLayer.ADDITIONAL_SYNTAX,
                    new TextAttributes(JBColor.BLACK, null, null, null, Font.PLAIN),
                    HighlighterTargetArea.LINES_IN_RANGE
            );

            highlighter.setGutterIconRenderer(button);
            sendButtonHighlighters.put(lineNumber, highlighter);
        }
    }
}