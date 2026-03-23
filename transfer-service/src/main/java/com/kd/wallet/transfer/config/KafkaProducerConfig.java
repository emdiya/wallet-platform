package com.kd.wallet.transfer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.wallet.transfer.event.TransferEvent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

	@Bean
	ProducerFactory<String, TransferEvent> transferEventProducerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> properties = kafkaProperties.buildProducerProperties();
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		JsonSerializer<TransferEvent> valueSerializer = new JsonSerializer<>(objectMapper);
		return new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), valueSerializer);
	}

	@Bean
	KafkaTemplate<String, TransferEvent> transferEventKafkaTemplate(
			ProducerFactory<String, TransferEvent> transferEventProducerFactory) {
		return new KafkaTemplate<>(transferEventProducerFactory);
	}
}
