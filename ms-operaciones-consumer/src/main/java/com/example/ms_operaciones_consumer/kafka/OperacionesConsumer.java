
package com.example.ms_operaciones_consumer.kafka;

import com.example.ms_operaciones_consumer.dto.TradeDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.Acknowledgment;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Slf4j
@Component
public class OperacionesConsumer {

    // Mapper solo para logging (JSON pretty + soporte fechas)
    private static final ObjectMapper LOG_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS);

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(
            @Payload TradeDto dto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Headers headers,
            Acknowledgment ack
    ) {
        long t0 = System.currentTimeMillis();

        // --- LOG DE ENTRADA, CAMPO A CAMPO ---
        log.info("[CONSUME][IN] topic={}, partition={}, offset={}, key={}, idTrade={}, monto={}, fechaCreacion={}, idCliente={}",
                topic, partition, offset, key,
                safe(() -> dto.getIdTrade()),
                safe(() -> dto.getMonto()),
                safe(() -> dto.getFechaCreacion()),
                safe(() -> dto.getIdCliente())
        );

        // (Tu lógica de negocio aquí)
        // procesar(dto);

        // --- LOG JSON COMPLETO DEL PAYLOAD (PRETTY) ---
        log.info("[CONSUME][PAYLOAD] {}", toPrettyJson(dto));

        // Commit manual si todo OK
        ack.acknowledge();
        long took = System.currentTimeMillis() - t0;

        log.info("[CONSUME][OK] topic={}, partition={}, offset={}, key={}, tookMs={}",
                topic, partition, offset, key, took);
    }

    // Helpers de logging seguros
    private static <T> T safe(SupplierWithEx<T> s) {
        try { return s.get(); } catch (Exception e) { return null; }
    }
    @FunctionalInterface private interface SupplierWithEx<T> { T get() throws Exception; }

    private static String toPrettyJson(Object o) {
        try { return LOG_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o); }
        catch (JsonProcessingException e) { return String.valueOf(o); }
    }
}