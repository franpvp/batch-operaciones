package com.example.bff_operaciones.consumer;

import com.example.bff_operaciones.dto.TradeDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DltTradeConsumer {

    /** ID estable para controlar el listener vía KafkaListenerEndpointRegistry */
    public static final String LISTENER_ID = "dlt-trade-listener";

    @Value("${app.kafka.dlt-topic}")
    private String dltTopic;

    @KafkaListener(
            id = LISTENER_ID,
            topics = "${app.kafka.dlt-topic}",
            containerFactory = "tradeKafkaListenerContainerFactory"
    )
    public void onBatch(List<ConsumerRecord<String, TradeDto>> records) {
        if (records == null || records.isEmpty()) return;

        for (ConsumerRecord<String, TradeDto> record : records) {
            final String topic = record.topic();
            final Integer partition = record.partition();
            final Long offset = record.offset();
            final String key = record.key();
            final TradeDto trade = record.value();

            System.out.printf(
                    "DLT -> topic=%s partition=%d offset=%d key=%s value=%s%n",
                    topic, partition, offset, key, trade
            );
        }
        // Con ack-mode=BATCH en config, si no lanzas excepción se comitean offsets del lote.
    }
}