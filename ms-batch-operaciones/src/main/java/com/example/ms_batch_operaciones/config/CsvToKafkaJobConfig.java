package com.example.ms_batch_operaciones.config;

import com.example.ms_batch_operaciones.model.TradeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Set;

@Slf4j
@Configuration
public class CsvToKafkaJobConfig {

    @Value("${app.kafka.topic}")
    private String topic;

    // ===== Reader: SOLO acepta el path pasado por el watcher =====
    @Bean
    @StepScope
    public FlatFileItemReader<TradeDto> tradeReader(
            @Value("#{jobParameters['app.csv.path']}") String csvPath) {

        if (csvPath == null || csvPath.isBlank()) {
            throw new IllegalStateException("Este job debe ejecutarse SOLO vía watcher: falta jobParameter 'app.csv.path'");
        }

        org.springframework.core.io.Resource resource;
        try {
            if (csvPath.startsWith("file:") || csvPath.startsWith("classpath:") || csvPath.startsWith("http")) {
                resource = new org.springframework.core.io.UrlResource(csvPath);
            } else {
                resource = new org.springframework.core.io.FileSystemResource(csvPath);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ruta CSV inválida: " + csvPath, e);
        }

        log.info("[READER] CSV recibido por watcher: path='{}' exists={}", csvPath, resource.exists());
        if (!resource.exists()) {
            throw new IllegalStateException("El CSV no existe o no es accesible: " + resource);
        }

        return new FlatFileItemReaderBuilder<TradeDto>()
                .name("tradeReader")
                .resource(resource)
                .strict(true)
                .encoding("UTF-8")
                .linesToSkip(1) // header
                .delimited()
                .names("idTrade", "monto", "canal", "fechaCreacion", "idCliente")
                .fieldSetMapper(fs -> {
                    // Parseo explícito para evitar problemas de conversión
                    var idTrade = fs.readLong("idTrade");
                    var monto = fs.readLong("monto");
                    var canal = fs.readString("canal");
                    var fecha = java.time.LocalDate.parse(fs.readString("fechaCreacion")); // ISO yyyy-MM-dd
                    var idCliente = fs.readLong("idCliente");
                    var t = new TradeDto();
                    t.setIdTrade(idTrade);
                    t.setMonto(monto);
                    t.setCanal(canal);
                    t.setFechaCreacion(fecha);
                    t.setIdCliente(idCliente);
                    return t;
                })
                .build();
    }

    @Bean
    public ItemProcessor<TradeDto, TradeDto> tradeProcessor(KafkaTemplate<String, TradeDto> kafkaTemplate) {

        final Set<String> CANALES_VALIDOS = Set.of("BANCO", "DATATEC", "PTO.VENTA", "INTEGRAL");
        final String DLQ_TOPIC = "operaciones-trade-topic.DLT";

        return t -> {
            if (t.getIdTrade() == null) {
                log.warn("[BATCH] Trade ignorado: idTrade nulo");
                return null;
            }
            if (t.getMonto() == null || t.getMonto() <= 0) {
                log.warn("[BATCH] Trade {} ignorado: monto inválido ({})", t.getIdTrade(), t.getMonto());
                return null;
            }
            if (t.getCanal() == null) {
                log.warn("[BATCH] Trade {} ignorado: canal nulo", t.getIdTrade());
                return null;
            }

            // ✅ Validación de canal
            if (!CANALES_VALIDOS.contains(t.getCanal().toUpperCase())) {
                log.error("[BATCH] Trade {} con canal inválido '{}'. Enviando a DLQ ({})",
                        t.getIdTrade(), t.getCanal(), DLQ_TOPIC);

                kafkaTemplate.send(DLQ_TOPIC, String.valueOf(t.getIdTrade()), t);
                return null;
            }

            if (t.getFechaCreacion() == null) {
                log.warn("[BATCH] Trade {} ignorado: fechaCreacion nula", t.getIdTrade());
                return null;
            }
            if (t.getIdCliente() == null) {
                log.warn("[BATCH] Trade {} ignorado: idCliente nulo", t.getIdTrade());
                return null;
            }

            return t;
        };
    }

    @Bean
    public ItemWriter<TradeDto> tradeKafkaWriter(KafkaTemplate<String, Object> kafkaTemplate) {
        return items -> {
            for (TradeDto t : items) {
                String key = String.valueOf(t.getIdCliente()); // o idTrade si prefieres
                kafkaTemplate.send(topic, key, t).whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("[KAFKA] ERROR publicando trade idTrade={} → {}", t.getIdTrade(), ex.toString(), ex);
                    } else {
                        log.info("[KAFKA] OK trade idTrade={} → topic='{}' partition={} offset={}",
                                t.getIdTrade(),
                                res.getRecordMetadata().topic(),
                                res.getRecordMetadata().partition(),
                                res.getRecordMetadata().offset());
                    }
                });
            }
            kafkaTemplate.flush();
            log.info("[KAFKA] {} trades despachados (flush) a '{}'", items.size(), topic);
        };
    }

    @Bean
    public Step csvToKafkaStep(JobRepository repo,
                               PlatformTransactionManager txManager,
                               FlatFileItemReader<TradeDto> reader,
                               ItemProcessor<TradeDto, TradeDto> processor,
                               ItemWriter<TradeDto> writer) {
        return new StepBuilder("csvToKafkaStep", repo)
                .<TradeDto, TradeDto>chunk(100, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .listener(new org.springframework.batch.core.listener.StepExecutionListenerSupport() {
                    @Override public void beforeStep(org.springframework.batch.core.StepExecution se) {
                        log.info("[BATCH] Iniciando step: {}", se.getStepName());
                    }
                    @Override public org.springframework.batch.core.ExitStatus afterStep(org.springframework.batch.core.StepExecution se) {
                        log.info("[BATCH] Finalizado step: {} (Leídos: {}, Filtrados: {}, Escritos: {})",
                                se.getStepName(), se.getReadCount(), se.getFilterCount(), se.getWriteCount());
                        return se.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public Job csvToKafkaJob(JobRepository repo, Step csvToKafkaStep) {
        return new JobBuilder("csvToKafkaJob", repo)
                .incrementer(new RunIdIncrementer())
                .start(csvToKafkaStep)
                .listener(new org.springframework.batch.core.listener.JobExecutionListenerSupport() {
                    @Override public void beforeJob(org.springframework.batch.core.JobExecution je) {
                        log.info("[BATCH] Iniciando job: {}", je.getJobInstance().getJobName());
                    }
                    @Override public void afterJob(org.springframework.batch.core.JobExecution je) {
                        log.info("[BATCH] Finalizado job: {} con estado: {}", je.getJobInstance().getJobName(), je.getStatus());
                    }
                })
                .build();
    }
}