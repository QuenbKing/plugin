package edu.kafkapractice.plugin.file.sendbutton;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import edu.kafkapractice.plugin.KafkaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class KafkaSendButton extends GutterIconRenderer {
    private final Editor editor;
    private final int lineNumber;

    public KafkaSendButton(Editor editor, int lineNumber) {
        this.editor = editor;
        this.lineNumber = lineNumber;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return KafkaIcons.SEND_ICON;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return "Send message to kafka";
    }

    @Nullable
    @Override
    public AnAction getClickAction() {
        return new KafkaSendAction(editor, lineNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        KafkaSendButton that = (KafkaSendButton) obj;
        return lineNumber == that.lineNumber && Objects.equals(editor, that.editor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(editor, lineNumber);
    }
}
