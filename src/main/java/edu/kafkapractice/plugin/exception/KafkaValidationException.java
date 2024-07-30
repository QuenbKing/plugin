package edu.kafkapractice.plugin.exception;

public class KafkaValidationException extends RuntimeException {

    public KafkaValidationException(String message) {
        super(message);
    }
}
