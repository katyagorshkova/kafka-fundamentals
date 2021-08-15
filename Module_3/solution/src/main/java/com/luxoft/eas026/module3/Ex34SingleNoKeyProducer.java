package com.luxoft.eas026.module3;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Ex34SingleNoKeyProducer {
    private final static String BOOTSTRAP_SERVERS = ":9092,:9093,:9094";
    private final static String TOPIC = "events4";
    private final static String CLIENT_ID = "ex34";
    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        final Producer<String, String> producer = new KafkaProducer<>(props);
        try {
            for (int index = 0; index < 11; index++) {
                final ProducerRecord<String, String> data = new ProducerRecord<>(TOPIC,
                        "m" + index);
                try {
                    RecordMetadata meta = producer.send(data).get();
                    System.out.printf("key=%s, value=%s => partition=%d, offset=%d\n",
                        data.key(), data.value(), meta.partition(), meta.offset());
                } catch (InterruptedException|ExecutionException e) {
                    System.out.printf("Exception %s\n", e.getMessage());
                }
            }
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
