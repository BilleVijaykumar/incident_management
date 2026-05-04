package com.ims.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration - Redis Streams alternative using Kafka
 */
@Configuration
public class KafkaConfig {
    
    public static final String SIGNAL_TOPIC = "ims-signals";
    public static final String SIGNAL_CONSUMER_GROUP = "ims-signal-consumer";
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        
        // Error handler with retry
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            new FixedBackOff(1000L, 3L) // 1 second backoff, 3 retries
        );
        factory.setCommonErrorHandler(errorHandler);
        
        return factory;
    }
    
    @Bean
    public org.apache.kafka.clients.admin.NewTopic signalTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", "604800000"); // 7 days
        configs.put("segment.bytes", "1073741824"); // 1GB
        
        return new org.apache.kafka.clients.admin.NewTopic(SIGNAL_TOPIC, 6, (short) 1)
            .configs(configs);
    }
}