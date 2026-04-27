package ru.linskay.ymv;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class ConfigService {
    private static final File FILE = new File("config.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AppConfig config;

    public ConfigService() {
        load();
    }

    public AppConfig get() {
        return config;
    }

    public void save(AppConfig cfg) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(FILE, cfg);
            config = cfg;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() {
        try {
            if (FILE.exists()) {
                config = MAPPER.readValue(FILE, AppConfig.class);
            } else {
                config = AppConfig.defaults();
                save(config);
            }
        } catch (Exception e) {
            config = AppConfig.defaults();
        }
    }
}
