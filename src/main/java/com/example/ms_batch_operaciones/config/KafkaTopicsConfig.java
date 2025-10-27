
package com.example.ms_batch_operaciones.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
  @Value("${app.kafka.topic}")
  private String topic;

  @Bean
  public NewTopic clientesTopic() {
    return TopicBuilder.name(topic)
        .partitions(3)
        .replicas(1)
        .build();
  }
}
