package edu.kafkapractice.plugin.file;

import com.intellij.openapi.fileTypes.LanguageFileType;
import edu.kafkapractice.plugin.KafkaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class KafkaFileType extends LanguageFileType {
    public static final KafkaFileType INSTANCE = new KafkaFileType();

    private KafkaFileType() {
        super(KafkaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Kafka File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Kafka configuration file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "kafka";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return KafkaIcons.FILE;
    }
}
