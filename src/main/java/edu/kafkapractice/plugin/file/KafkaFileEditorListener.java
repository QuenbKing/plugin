package edu.kafkapractice.plugin.file;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import edu.kafkapractice.plugin.file.client.KafkaClientManager;
import org.jetbrains.annotations.NotNull;

public class KafkaFileEditorListener implements FileEditorManagerListener {
    private final KafkaFileParser kafkaFileParser;

    public KafkaFileEditorListener(KafkaFileParser kafkaFileParser) {
        this.kafkaFileParser = kafkaFileParser;
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        Editor editor = event.getManager().getSelectedTextEditor();
        VirtualFile newFile = event.getNewFile();

        if (editor != null && newFile != null && newFile.getName().endsWith(".kafka")) {
            kafkaFileParser.parseKafkaFile(editor);
        }
        KafkaClientManager.closeConnection();
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (file.getName().endsWith(".kafka")) {
            kafkaFileParser.removeEditorListener(file);
        }
    }
}
