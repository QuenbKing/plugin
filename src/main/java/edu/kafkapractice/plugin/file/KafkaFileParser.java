package edu.kafkapractice.plugin.file;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import edu.kafkapractice.plugin.file.sendbutton.SendButtonDocumentListener;

import java.util.HashMap;
import java.util.Map;

public class KafkaFileParser {
    private final Map<Document, SendButtonDocumentListener> documentListeners = new HashMap<>();

    public void parseKafkaFile(Editor editor) {
        Document document = editor.getDocument();

        if (!documentListeners.containsKey(document)) {
            SendButtonDocumentListener listener = new SendButtonDocumentListener(editor);
            document.addDocumentListener(listener);
            documentListeners.put(document, listener);
            listener.addSendButtons();
        }
    }

    public void removeEditorListener(VirtualFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return;
        }

        SendButtonDocumentListener listener = documentListeners.remove(document);
        if (listener != null) {
            document.removeDocumentListener(listener);
        }
    }
}
