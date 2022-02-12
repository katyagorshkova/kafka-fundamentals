package kafkatests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest()
@EmbeddedKafka(partitions = 5, topics = { "topic2" })
class KafkaConsumerServiceTest {

	@SpyBean
	private KafkaMessageConsumerService consumerService;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Captor
	private ArgumentCaptor<String> payLoadArgumentCaptor;

	@Captor
	private ArgumentCaptor<String> topicArgumentCaptor;

	@Captor
	private ArgumentCaptor<Integer> partitionArgumentCaptor;

	@Captor
	private ArgumentCaptor<Long> offsetArgumentCaptor;

	private Producer<String, String> producer;

	@BeforeEach
	public void setUp() {
		Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
		producer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer())
				.createProducer();
	}

	@Test
	public void shouldReadMessage() throws InterruptedException, ExecutionException {

		producer.send(new ProducerRecord<>("topic2", 0, null, "msg2"));
		producer.flush();

		verify(consumerService, timeout(50000).times(1)).onMessage(payLoadArgumentCaptor.capture(),
				topicArgumentCaptor.capture(), partitionArgumentCaptor.capture(), offsetArgumentCaptor.capture());

		assertEquals("msg2", payLoadArgumentCaptor.getValue());
		assertEquals("topic2", topicArgumentCaptor.getValue());
		assertEquals(0, partitionArgumentCaptor.getValue());
		assertEquals(0, offsetArgumentCaptor.getValue());
	}

	@AfterEach
	void shutdown() {
		producer.close();
	}
}