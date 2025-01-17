package edu.kafkapractice.plugin.file.sendbutton;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import edu.kafkapractice.plugin.KafkaMessageDialog;
import edu.kafkapractice.plugin.file.client.KafkaClientManager;
import org.jetbrains.annotations.NotNull;

public class KafkaSendAction extends AnAction {
    private final Editor editor;
    private final int lineNumber;

    public KafkaSendAction(Editor editor, int lineNumber) {
        this.editor = editor;
        this.lineNumber = lineNumber;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        Project project = e.getProject();
        if (project != null && file != null) {
            KafkaClientManager kafkaClientManager = KafkaClientManager.getInstance(project);
            KafkaMessageDialog dialog = new KafkaMessageDialog(editor, lineNumber, kafkaClientManager);
            dialog.show();
        }
    }
}
