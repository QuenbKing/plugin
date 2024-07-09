package edu.kafkapractice.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
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
        KafkaFileEditorListener.registerListener(project);

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();
        if (selectedFiles.length > 0) {
            VirtualFile activeFile = selectedFiles[0];
            if (activeFile != null && activeFile.getName().endsWith(".kafka")) {
                ApplicationManager.getApplication().invokeLater(() -> KafkaFileParser.parseKafkaFile(activeFile, project));
            }
        }

        return Unit.INSTANCE;
    }
}