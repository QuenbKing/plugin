package edu.kafkapractice.plugin.file;

import com.intellij.lang.Language;

public class KafkaLanguage extends Language {
    public static final KafkaLanguage INSTANCE = new KafkaLanguage();

    private KafkaLanguage() {
        super("Kafka");
    }
}
