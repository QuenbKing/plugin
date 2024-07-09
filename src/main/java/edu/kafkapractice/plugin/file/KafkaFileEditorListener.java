package edu.kafkapractice.plugin.file;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

public class KafkaFileEditorListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile newFile = event.getNewFile();
        Project project = event.getManager().getProject();

        if (newFile != null && newFile.getName().endsWith(".kafka")) {
            KafkaFileParser.parseKafkaFile(newFile, project);
        }
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//        if (file.getName().endsWith(".kafka")) {
//            Project project = source.getProject();
//            ApplicationManager.getApplication().invokeLater(() -> KafkaFileParser.parseKafkaFile(file, project));
//        }
    }


    public static void registerListener(Project project) {
        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new KafkaFileEditorListener());
    }
}
