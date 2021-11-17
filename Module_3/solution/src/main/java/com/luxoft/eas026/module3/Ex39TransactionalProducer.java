package com.luxoft.eas026.module3;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Ex39TransactionalProducer {
	private final static String BOOTSTRAP_SERVERS = ":9092,:9093,:9094";
	private final static String TOPIC1 = "topic1";
	private final static String TOPIC2 = "topic2";
	private final static String CLIENT_ID = "ex37";

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my.id");

		final Producer<String, String> producer = new KafkaProducer<>(props);
		producer.initTransactions();
		try {
			producer.beginTransaction();
			final ProducerRecord<String, String> data1 = new ProducerRecord<>(TOPIC1, "m11");
			final ProducerRecord<String, String> data2 = new ProducerRecord<>(TOPIC2, "m22");
			try {
				RecordMetadata meta = producer.send(data1).get();
				System.out.printf("key=%s, value=%s => partition=%d, offset=%d\n", data1.key(), data1.value(),
						meta.partition(), meta.offset());
				RecordMetadata meta2 = producer.send(data2).get();
				System.out.printf("key=%s, value=%s => partition=%d, offset=%d\n", data2.key(), data2.value(),
						meta2.partition(), meta2.offset());

				producer.commitTransaction();
			} catch (InterruptedException | ExecutionException e) {
				System.out.printf("Exception %s\n", e.getMessage());
			}

		} finally {
			producer.flush();
			producer.close();
		}
	}
}
