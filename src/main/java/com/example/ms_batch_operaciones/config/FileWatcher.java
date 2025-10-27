package com.example.ms_batch_operaciones.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@Slf4j
@Component("csvFileWatcher")
public class FileWatcher {

    private final JobLauncher jobLauncher;
    private final Job csvToKafkaJob;

    private final Path inbox;
    private final Path processed;
    private final Path error;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "csv-file-watcher");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean running = true;

    public FileWatcher(
            JobLauncher jobLauncher,
            Job csvToKafkaJob,
            @Value("${app.watch.inbox:./inbox}") String inboxDir,
            @Value("${app.watch.processed:./processed}") String processedDir,
            @Value("${app.watch.error:./error}") String errorDir) {

        this.jobLauncher = jobLauncher;
        this.csvToKafkaJob = csvToKafkaJob;
        this.inbox = Paths.get(inboxDir).toAbsolutePath();
        this.processed = Paths.get(processedDir).toAbsolutePath();
        this.error = Paths.get(errorDir).toAbsolutePath();

        try {
            Files.createDirectories(inbox);
            Files.createDirectories(processed);
            Files.createDirectories(error);
            log.info("[WATCHER] Directorios OK → inbox={}, processed={}, error={}",
                    this.inbox, this.processed, this.error);
        } catch (IOException e) {
            throw new RuntimeException("No se pudieron crear directorios del watcher", e);
        }
    }

    /** Arranca el watcher cuando la app está lista */
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        log.info("[WATCHER] Iniciando watcher sobre {}", inbox);
        executor.submit(this::watchLoop);
    }

    @PreDestroy
    public void stop() {
        running = false;
        executor.shutdownNow();
        log.info("[WATCHER] Detenido");
    }

    private void watchLoop() {
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            inbox.register(ws, ENTRY_CREATE);
            while (running) {
                WatchKey key = ws.take(); // bloquea hasta que se ingrese un valor
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == ENTRY_CREATE) {
                        Path created = inbox.resolve((Path) event.context());
                        log.info("[WATCHER] Detectado nuevo archivo: {}", created.getFileName());
                        // Procesa en otro hilo para no bloquear el loop
                        handleNewFile(created);
                    }
                }
                if (!key.reset()) break;
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[WATCHER] Interrumpido");
        } catch (Exception e) {
            log.error("[WATCHER] Error inesperado en loop", e);
        }
    }

    private void handleNewFile(Path path) {
        try {
            if (!isCsv(path)) {
                log.debug("[WATCHER] Ignorado (no CSV): {}", path.getFileName());
                return;
            }

            // Espera a que el archivo deje de crecer
            waitUntilStable(path, 4, 250);

            URI uri = path.toUri();
            JobParameters params = new JobParametersBuilder()
                    .addString("app.csv.path", uri.toString())
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            log.info("[WATCHER] Lanzando job csvToKafkaJob con archivo: {}", uri);
            jobLauncher.run(csvToKafkaJob, params);
            log.info("[WATCHER] Job lanzado OK para: {}", uri);

            Path target = processed.resolve(stampedName(path.getFileName().toString()));
            Files.move(path, target, REPLACE_EXISTING);
            log.info("[WATCHER] Archivo {} procesado → movido a '{}'", path.getFileName(), target.getParent());

        } catch (Exception ex) {
            try {
                Files.move(path, error.resolve(path.getFileName()), REPLACE_EXISTING);
            } catch (Exception ignore) {
                log.warn("[WATCHER] No se pudo mover a carpeta error: {}", path.getFileName());
            }
            log.error("[WATCHER] ERROR procesando {} → movido a carpeta 'error'", path.getFileName(), ex);
        }
    }

    private static boolean isCsv(Path p) {
        String name = Objects.toString(p.getFileName(), "").toLowerCase();
        return name.endsWith(".csv");
    }

    /** Espera a que el tamaño del archivo quede estable (evita leerlo mientras se está copiando) */
    private static void waitUntilStable(Path p, int checks, long sleepMs) throws IOException, InterruptedException {
        long prev = -1;
        for (int i = 0; i < checks; i++) {
            long size = Files.size(p);
            if (size > 0 && size == prev) return; // estable
            prev = size;
            Thread.sleep(sleepMs);
        }
    }

    private static String stampedName(String original) {
        int dot = original.lastIndexOf('.');
        String base = dot > 0 ? original.substring(0, dot) : original;
        String ext = dot > 0 ? original.substring(dot) : "";
        return base + "_" + Instant.now().toEpochMilli() + ext;
    }
}