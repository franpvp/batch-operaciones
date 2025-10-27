package com.example.ms_batch_operaciones.config;

import com.example.ms_batch_operaciones.model.ClienteDto;
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
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class CsvToKafkaJobConfig {

  @Value("${app.csv.path}")
  private Resource csvResource;

  @Value("${app.kafka.topic}")
  private String topic;

  @Bean
  @StepScope
  public FlatFileItemReader<ClienteDto> clienteReader(
          @Value("#{jobParameters['app.csv.path'] ?: '${app.csv.path}'}") Resource csvResource) {

    var mapper = new BeanWrapperFieldSetMapper<ClienteDto>();
    mapper.setTargetType(ClienteDto.class);

    return new FlatFileItemReaderBuilder<ClienteDto>()
            .name("clienteReader")
            .resource(csvResource)
            .linesToSkip(1)
            .delimited()
            .names("id", "nombre", "correo", "edad")
            .fieldSetMapper(mapper)
            .build();
  }

  @Bean
  public ItemProcessor<ClienteDto, ClienteDto> clienteProcessor() {
    return item -> {
      if (item.getCorreo() == null || item.getCorreo().isBlank()) {
        log.warn("[BATCH] Cliente con ID={} ignorado: correo vacío o nulo", item.getId());
        return null;
      }
      return item;
    };
  }

  @Bean
  public ItemWriter<ClienteDto> clienteKafkaWriter(KafkaTemplate<String, Object> kafkaTemplate) {
    return items -> {
      for (ClienteDto c : items) {
        kafkaTemplate.send(topic, c.getId(), c);
        log.info("[KAFKA] Enviado cliente id={} nombre={} al tópico '{}'", c.getId(), c.getNombre(), topic);
      }
      kafkaTemplate.flush();
      log.info("[KAFKA] {} mensajes enviados correctamente al tópico '{}'", items.size(), topic);
    };
  }

  @Bean
  public Step csvToKafkaStep(JobRepository repo,
                             PlatformTransactionManager txManager,
                             FlatFileItemReader<ClienteDto> reader,
                             ItemProcessor<ClienteDto, ClienteDto> processor,
                             ItemWriter<ClienteDto> writer) {
    return new StepBuilder("csvToKafkaStep", repo)
            .<ClienteDto, ClienteDto>chunk(100, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(Exception.class)
            .skipLimit(50)
            .listener(new org.springframework.batch.core.listener.StepExecutionListenerSupport() {
              @Override
              public void beforeStep(org.springframework.batch.core.StepExecution stepExecution) {
                log.info("[BATCH] Iniciando step: {}", stepExecution.getStepName());
              }

              @Override
              public org.springframework.batch.core.ExitStatus afterStep(org.springframework.batch.core.StepExecution stepExecution) {
                log.info("[BATCH] Finalizado step: {} (Leídos: {}, Escritos: {})",
                        stepExecution.getStepName(),
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount());
                return stepExecution.getExitStatus();
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
              @Override
              public void beforeJob(org.springframework.batch.core.JobExecution jobExecution) {
                log.info("[BATCH] Iniciando job: {}", jobExecution.getJobInstance().getJobName());
              }

              @Override
              public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
                log.info("[BATCH] Finalizado job: {} con estado: {}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStatus());
              }
            })
            .build();
  }
}