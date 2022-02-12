package com.luxoft.eas026.module3;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaApplication {

	private static final Logger log = LoggerFactory.getLogger(KafkaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);
    }
    
    @Bean
    public Supplier<String> send() {
        return () ->  String.valueOf(new Random().nextInt());
    }

    @Bean
    public Consumer<String> receive() {
        return message -> log.info("Message " + message);
    }
}
