package edu.kafkapractice.plugin;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;

public class KafkaClient {

    private final KafkaProducer<String, String> producer;

    public KafkaClient(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String key, String message, Map<String, String> headers) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        headers.forEach((headerKey, headerValue) -> record.headers().add(headerKey, headerValue.getBytes()));
        producer.send(record);
    }

    public void close() {
        producer.close();
    }
}