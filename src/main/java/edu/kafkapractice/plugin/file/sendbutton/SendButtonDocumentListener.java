package edu.kafkapractice.plugin.file.sendbutton;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SendButtonDocumentListener implements DocumentListener {

    private static final String SEND_MESSAGE_MARKER = "###Send Message";

    private final Editor editor;
    private final Map<Integer, RangeHighlighter> sendButtonHighlighters = new HashMap<>();

    private int previousDocumentLinesCount;

    public SendButtonDocumentListener(Editor editor) {
        this.editor = editor;
        this.previousDocumentLinesCount = editor.getDocument().getLineCount();
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent e) {
        int startLine = editor.getDocument().getLineNumber(e.getOffset());
        int linesDeleted = previousDocumentLinesCount - editor.getDocument().getLineCount();

        if (linesDeleted == 0) {
            updateSendButtonForLine(startLine);
        } else {
            int removeEndLine = linesDeleted > 0
                    ? editor.getDocument().getLineCount() + linesDeleted
                    : editor.getDocument().getLineCount();
            updateSendButtonsInRange(startLine, removeEndLine);
        }

        previousDocumentLinesCount = editor.getDocument().getLineCount();
    }

    public void addSendButtons() {
        String content = editor.getDocument().getText();
        String[] lines = content.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(SEND_MESSAGE_MARKER) && !sendButtonHighlighters.containsKey(i)) {
                installSendButton(i);
            }
        }
    }

    private void updateSendButtonsInRange(int startLine, int removeEndLine) {
        for (int i = startLine; i < removeEndLine; i++) {
            updateSendButtonForLine(i);
        }
    }

    private void updateSendButtonForLine(int lineNumber) {
        if (lineNumber < 0 || lineNumber >= editor.getDocument().getLineCount()) {
            removeSendButton(lineNumber);
            return;
        }

        String lineText = getLineText(lineNumber);

        if (sendButtonHighlighters.containsKey(lineNumber)) {
            removeSendButton(lineNumber);
        }

        if (lineText.startsWith(SEND_MESSAGE_MARKER)) {
            installSendButton(lineNumber);
        }
    }

    private String getLineText(int lineNumber) {
        int startOffset = editor.getDocument().getLineStartOffset(lineNumber);
        int endOffset = editor.getDocument().getLineEndOffset(lineNumber);
        return editor.getDocument().getText().substring(startOffset, endOffset);
    }

    private void installSendButton(int lineNumber) {
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

    private void removeSendButton(int lineNumber) {
        RangeHighlighter highlighter = sendButtonHighlighters.get(lineNumber);
        if (highlighter != null) {
            editor.getMarkupModel().removeHighlighter(highlighter);
            sendButtonHighlighters.remove(lineNumber);
        }
    }
}