package com.luxoft.eas026.module3;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class SlowConsumer {

	private final static String BOOTSTRAP_SERVERS = ":9092,:9093,:9094";
	private static final String GROUP_ID = "ex";
	private final static String OFFSET_RESET = "earliest";

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 5000);
		final KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props);

		try {
			consumer.subscribe(Collections.singleton(args[0]), new ConsumerRebalanceListener() {

				@Override
				public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
					System.out.printf("onPartitionsRevoked - partitions: %s%n", formatPartitions(partitions));
				}

				@Override
				public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
					System.out.printf("onPartitionsAssigned - partitions: %s%n", formatPartitions(partitions));

				}
			});
			while (true) {
				ConsumerRecords<String, Integer> records = consumer.poll(Duration.ofSeconds(2));
				if (!records.isEmpty()) {
					for (ConsumerRecord<String, Integer> data : records) {
						System.out.printf("key=%s, value=%s => partition=%d, offset=%d\n", data.key(), data.value(),
								data.partition(), data.offset());
					}
					if (Boolean.valueOf(args[1])) {
						Thread.sleep(Integer.MAX_VALUE);
					}
				}
			}
		} catch (Exception e) {
			System.out.printf("Exception %s\n", e.getMessage());
		} finally {
			consumer.close();
		}
	}

	public static List<String> formatPartitions(Collection<TopicPartition> partitions) {
		return partitions.stream().map(topicPartition -> String.format("topic: %s, partition: %s",
				topicPartition.topic(), topicPartition.partition())).collect(Collectors.toList());
	}
}
