package edu.kafkapractice.plugin.file;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import edu.kafkapractice.plugin.file.sendbutton.SendButtonDocumentListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KafkaFileParser {

    private static final Map<Editor, SendButtonDocumentListener> editorListeners = new HashMap<>();

    public static void parseKafkaFile(VirtualFile file, Project project) {
        Editor[] editors = EditorFactory.getInstance().getEditors(Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(file)), project);
        if (editors.length == 0) {
            return;
        }

        Editor editor = editors[0];

        if (!editorListeners.containsKey(editor)) {
            String content = editor.getDocument().getText();
            String[] lines = content.split("\\r?\\n");
            SendButtonDocumentListener listener = new SendButtonDocumentListener(editor);
            editor.getDocument().addDocumentListener(listener);
            editorListeners.put(editor, listener);
            listener.addSendButtons(lines);
        }
    }
}
