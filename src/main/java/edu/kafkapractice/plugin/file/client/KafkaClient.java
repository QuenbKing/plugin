package edu.kafkapractice.plugin.file.client;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaClient {

    private KafkaProducer<String, String> producer;
    private final String bootstrapServers;

    public KafkaClient(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
        connect(bootstrapServers);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void sendMessage(String topic, String key, String message, Map<String, String> headers) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        headers.forEach((headerKey, headerValue) -> record.headers().add(headerKey, headerValue.getBytes()));
        producer.send(record).get();
    }

    public void close(Duration duration) {
        if (producer != null) {
            producer.close(duration);
        }
    }

    private void connect(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10000);
        producer = new KafkaProducer<>(props);
    }
}