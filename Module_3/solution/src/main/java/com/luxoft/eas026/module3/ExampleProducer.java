package com.luxoft.eas026.module3;

import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class ExampleProducer {

	private final static String BOOTSTRAP_SERVERS = ":9092,:9093,:9094";

	private final static String TOPIC = "events";

	private final static String CLIENT_ID = "ex";

	public static void main(String[] args){
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		final Producer<String, Integer> producer = new KafkaProducer<>(props);
		

		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				final int number = new Random().nextInt(10);
				ProducerRecord<String, Integer> data = new ProducerRecord<>(TOPIC, "key" + number, number);
				RecordMetadata meta;
				try {
					meta = producer.send(data).get();
					System.out.printf("key=%s, value=%s => partition=%d, offset=%d\n", data.key(), data.value(),
							meta.partition(), meta.offset());
				} catch (InterruptedException | ExecutionException e) {
					producer.flush();
				}
			
			}
		}, 0, 3000);

		Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
		
	}
}
