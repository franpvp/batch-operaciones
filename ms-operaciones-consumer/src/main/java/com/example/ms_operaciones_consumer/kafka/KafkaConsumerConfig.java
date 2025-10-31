package com.example.ms_operaciones_consumer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        f.setConsumerFactory(consumerFactory);

        // Ack manual
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Error handler
        f.setCommonErrorHandler(errorHandler);

        return f;
    }

    // ProducerFactory/KafkaTemplate para publicar a la DLT (auto-config via spring.kafka.producer.*)
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        var backoff = new FixedBackOff(1000L, 3L);
        var recoverer = new DeadLetterPublishingRecoverer(
                template,
                (record, ex) -> {
                    String dlt = record.topic() + ".DLT";
                    log.error("[DLT] Fallo definitivo topic={}, partition={}, offset={}, error={}",
                            record.topic(), record.partition(), record.offset(), ex.getMessage(), ex);
                    return new TopicPartition(dlt, record.partition());
                }
        );
        return new DefaultErrorHandler(recoverer, backoff);
    }

    @Bean
    public NewTopic operacionesTopic(
            @Value("${app.kafka.topic}") String topic,
            @Value("${app.kafka.partitions:3}") int partitions,
            @Value("${app.kafka.replication:1}") short replication
    ) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replication).build();
    }

    @Bean
    public NewTopic operacionesDltTopic(
            @Value("${app.kafka.topic}") String topic,
            @Value("${app.kafka.partitions:3}") int partitions,
            @Value("${app.kafka.replication:1}") short replication
    ) {
        return TopicBuilder.name(topic + ".DLT").partitions(partitions).replicas(replication).build();
    }
}