package ru.linskay.ymv.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private AppConfig config;

    public ConfigService() {
        load();
    }

    public AppConfig get() {
        return config;
    }

    public synchronized void save(AppConfig cfg) {
        Path configPath = ConfigPaths.getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            MAPPER.writeValue(configPath.toFile(), cfg);
            this.config = cfg;
            logger.info("Configuration saved to {}", configPath);
        } catch (IOException e) {
            logger.error("Failed to save configuration to " + configPath, e);
        }
    }

    private void load() {
        Path configPath = ConfigPaths.getConfigPath();
        try {
            if (Files.exists(configPath)) {
                try {
                    this.config = MAPPER.readValue(configPath.toFile(), AppConfig.class);
                    logger.info("Configuration loaded from {}", configPath);
                } catch (IOException e) {
                    logger.error("Configuration file is corrupted: " + configPath + ". Creating backup and restoring defaults.", e);
                    backupCorruptedConfig(configPath);
                    this.config = AppConfig.defaults();
                    save(this.config);
                }
            } else {
                logger.info("Configuration file not found, using defaults");
                this.config = AppConfig.defaults();
                save(this.config);
            }
        } catch (Exception e) {
            logger.error("Unexpected error loading configuration, using defaults", e);
            this.config = AppConfig.defaults();
        }
    }

    private void backupCorruptedConfig(Path configPath) {
        try {
            Path backupPath = configPath.resolveSibling("config.broken." + System.currentTimeMillis() + ".json");
            Files.move(configPath, backupPath);
            logger.info("Corrupted config backed up to {}", backupPath);
        } catch (IOException e) {
            logger.error("Failed to backup corrupted config", e);
        }
    }
}
