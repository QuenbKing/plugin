package edu.kafkapractice.plugin.file.client;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class KafkaClient {

    private KafkaProducer<String, String> producer;

    public KafkaClient(String bootstrapServers) throws TimeoutException {
        try {
            connect(bootstrapServers);
        } catch (KafkaException e) {
            throw new TimeoutException(e.getMessage());
        }
    }

    private void connect(String bootstrapServers) {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            adminClient.describeCluster().nodes().get();
        } catch (Exception e) {
            throw new KafkaException(bootstrapServers);
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producer = new KafkaProducer<>(props);
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