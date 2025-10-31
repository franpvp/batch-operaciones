package com.example.bff_operaciones.controller.kafka;

import com.example.bff_operaciones.consumer.DltTradeConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/kafka/listener")
public class KafkaListenerController {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaListenerController(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    private Optional<MessageListenerContainer> container() {
        // Usa el ID del listener definido en DltTradeConsumer
        return Optional.ofNullable(registry.getListenerContainer(DltTradeConsumer.LISTENER_ID));
    }

    @PostMapping("/start")
    public ResponseEntity<String> start() {
        return container().map(c -> {
            if (!c.isRunning()) c.start();
            return ResponseEntity.ok("Listener started");
        }).orElseGet(() -> ResponseEntity.badRequest().body("Listener not found"));
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        return container().map(c -> {
            if (c.isRunning()) c.stop();
            return ResponseEntity.ok("Listener stopped");
        }).orElseGet(() -> ResponseEntity.badRequest().body("Listener not found"));
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pause() {
        return container().map(c -> {
            c.pause();
            return ResponseEntity.ok("Listener paused");
        }).orElseGet(() -> ResponseEntity.badRequest().body("Listener not found"));
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resume() {
        return container().map(c -> {
            c.resume();
            return ResponseEntity.ok("Listener resumed");
        }).orElseGet(() -> ResponseEntity.badRequest().body("Listener not found"));
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return container().map(c -> {
            String msg = "running=" + c.isRunning() + ", paused=" + c.isPauseRequested();
            return ResponseEntity.ok(msg);
        }).orElseGet(() -> ResponseEntity.badRequest().body("Listener not found"));
    }
}
