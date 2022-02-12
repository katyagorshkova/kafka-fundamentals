package kafkatests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.assertj.KafkaConditions.key;
import static org.springframework.kafka.test.assertj.KafkaConditions.value;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest()
@EmbeddedKafka(partitions = 5, topics = { "topic1" })
public class KafkaProducerServiceTest {

	private static final Logger log = LoggerFactory.getLogger(KafkaProducerServiceTest.class);

	@Autowired
	private KafkaMessageProducerService kafkaMessageProducerService;

	private KafkaMessageListenerContainer<String, String> listener;

	private BlockingQueue<ConsumerRecord<String, String>> consumerRecords;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafka;

	@BeforeEach
	public void setUp() {
		consumerRecords = new LinkedBlockingQueue<>();

		ContainerProperties containerProperties = new ContainerProperties("topic1");

		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps("group1", "false",
				embeddedKafka);

		DefaultKafkaConsumerFactory<String, String> consumer = new DefaultKafkaConsumerFactory<>(consumerProperties);

		listener = new KafkaMessageListenerContainer<>(consumer, containerProperties);
		listener.setupMessageListener((MessageListener<String, String>) record -> {
			log.debug("Listened message='{}'", record.toString());
			consumerRecords.add(record);
		});
		listener.start();

		ContainerTestUtils.waitForAssignment(listener, embeddedKafka.getPartitionsPerTopic());
	}

	@AfterEach
	public void tearDown() {
		listener.stop();
	}

	@Test
	public void shouldSendMessage() throws InterruptedException, IOException {

		kafkaMessageProducerService.send("msg1");

		ConsumerRecord<String, String> received = consumerRecords.poll(10, TimeUnit.SECONDS);

		assertThat(received).has(value("msg1"));

		assertThat(received).has(key(null));
	}

}