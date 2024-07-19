package edu.kafkapractice.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import edu.kafkapractice.plugin.file.KafkaFileEditorListener;
import edu.kafkapractice.plugin.file.KafkaFileParser;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KafkaProjectActivity implements ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        KafkaFileParser kafkaFileParser = new KafkaFileParser();

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        Editor editor = fileEditorManager.getSelectedTextEditor();
        VirtualFile activeFile = editor != null ? editor.getVirtualFile() : null;

        if (activeFile != null && activeFile.getName().endsWith(".kafka")) {
            ApplicationManager.getApplication().invokeLater(() -> kafkaFileParser.parseKafkaFile(editor));
        }

        registerListener(project, kafkaFileParser);

        return Unit.INSTANCE;
    }

    private void registerListener(Project project, KafkaFileParser kafkaFileParser) {
        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new KafkaFileEditorListener(kafkaFileParser));
    }
}