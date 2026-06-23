package com.autome.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class AutoMeConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("autome.json");

    public boolean enabled = false;
    public String prefix = "/me";

    private static AutoMeConfig instance;

    public static AutoMeConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    public static AutoMeConfig load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) {
                instance = GSON.fromJson(r, AutoMeConfig.class);
                if (instance == null) instance = new AutoMeConfig();
                return instance;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        instance = new AutoMeConfig();
        return instance;
    }

    public void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
