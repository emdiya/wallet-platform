package com.kd.wallet.wallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.wallet.wallet.event.WalletEvent;
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
	ProducerFactory<String, WalletEvent> walletEventProducerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> properties = kafkaProperties.buildProducerProperties();
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		JsonSerializer<WalletEvent> valueSerializer = new JsonSerializer<>(objectMapper);
		return new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), valueSerializer);
	}

	@Bean
	KafkaTemplate<String, WalletEvent> walletEventKafkaTemplate(
			ProducerFactory<String, WalletEvent> walletEventProducerFactory) {
		return new KafkaTemplate<>(walletEventProducerFactory);
	}
}
