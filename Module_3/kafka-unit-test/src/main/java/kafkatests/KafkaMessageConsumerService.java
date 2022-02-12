package kafkatests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageConsumerService {

	private static final Logger log = LoggerFactory.getLogger(KafkaMessageConsumerService.class);

	@KafkaListener(topics = "topic2")
	public void onMessage(@Payload String msg,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {
		System.out.print("bubu===" + msg);
			log.info(msg);
	}
}