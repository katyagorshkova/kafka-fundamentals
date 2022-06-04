package com.luxoft.eas026.module3;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalProducer {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionalProducer.class);

	private static final String BOOTSTRAP_SERVERS = ":9092,:9093,:9094";
	private static final String TOPIC1 = "topic1";
	private static final String TOPIC2 = "topic2";
	private static final String CLIENT_ID = "ex37";

	@SuppressWarnings("boxing")
	public static void main(String[] args) {

		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my.id");

		try (Producer<String, Integer> producer = new KafkaProducer<>(props)) {
			producer.initTransactions();
			producer.beginTransaction();
			final ProducerRecord<String, Integer> data1 = new ProducerRecord<>(TOPIC1, 100);
			final ProducerRecord<String, Integer> data2 = new ProducerRecord<>(TOPIC2, 200);
			try {
				RecordMetadata meta1 = producer.send(data1).get();
				LOG.info("key = {}, value = {} => partition = {}, offset= {}", data1.key(), data1.value(), meta1.partition(), meta1.offset());
				RecordMetadata meta2 = producer.send(data2).get();
				LOG.info("key = {}, value = {} => partition = {}, offset= {}", data2.key(), data2.value(), meta2.partition(), meta2.offset());

				producer.commitTransaction();
			} catch (InterruptedException | ExecutionException e) {
				producer.abortTransaction();
				LOG.error("Something goes wrong: {}", e.getMessage(), e);
			} finally {
				producer.flush();
			}
		}
	}
}
