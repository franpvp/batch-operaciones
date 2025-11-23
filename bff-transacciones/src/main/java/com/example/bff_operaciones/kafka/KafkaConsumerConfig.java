package com.example.bff_operaciones.kafka;

import com.example.bff_operaciones.dto.TradeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * ConsumerFactory con JsonDeserializer<T> y ObjectMapper que soporta LocalDate.
     */
    @Bean
    public ConsumerFactory<String, TradeDto> tradeConsumerFactory(ObjectProvider<ObjectMapper> omProvider) {
        ObjectMapper mapper = omProvider.getIfAvailable(ObjectMapper::new);
        mapper.registerModule(new JavaTimeModule());

        JsonDeserializer<TradeDto> jsonDeserializer =
                new JsonDeserializer<>(TradeDto.class, mapper, false);
        jsonDeserializer.addTrustedPackages("com.example.bff_operaciones.dto");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // IMPORTANTE: pasar el deserializador como instancia para usar nuestro ObjectMapper
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    /**
     * Listener factory en modo batch; los headers NO se inyectan, usamos metadata del ConsumerRecord.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TradeDto> tradeKafkaListenerContainerFactory(
            ConsumerFactory<String, TradeDto> tradeConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TradeDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(tradeConsumerFactory);
        factory.setBatchListener(true); // entregará List<ConsumerRecord<...>>
        // Ack-mode BATCH se toma desde spring.kafka.listener.ack-mode, o puedes setearlo aquí si prefieres.
        // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        return factory;
    }
}