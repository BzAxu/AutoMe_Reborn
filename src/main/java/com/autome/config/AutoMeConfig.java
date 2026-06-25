package com.autome.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AutoMeConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir().resolve("autome");

    public boolean enabled = false;
    public String prefix = "/me";
    public List<String> history = new ArrayList<>();
    public List<String> pinned = new ArrayList<>();
    public List<String> filters = new ArrayList<>(
            Arrays.asList("/", ".", "#", "$", "&", "*", "@"));

    private static final Map<String, AutoMeConfig> cache = new HashMap<>();
    private static String currentKey = "default";

    public static void setCurrentKey(String key) {
        currentKey = key == null || key.isEmpty() ? "default" : key.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static String getCurrentKey() { return currentKey; }

    public static AutoMeConfig get() {
        return cache.computeIfAbsent(currentKey, k -> load(k));
    }

    private static Path pathFor(String key) {
        try { Files.createDirectories(CONFIG_DIR); } catch (IOException ignored) {}
        return CONFIG_DIR.resolve(key + ".json");
    }

    public static AutoMeConfig load(String key) {
        File file = pathFor(key).toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) {
                AutoMeConfig cfg = GSON.fromJson(r, AutoMeConfig.class);
                if (cfg != null) return cfg;
            } catch (IOException e) { e.printStackTrace(); }
        }
        return new AutoMeConfig();
    }

    public void save() {
        try (Writer w = new FileWriter(pathFor(currentKey).toFile())) {
            GSON.toJson(this, w);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // 添加历史记录（去重，置顶排前，最多20条）
    public void addHistory(String p) {
        history.remove(p);
        if (!pinned.contains(p)) {
            history.add(0, p);
            if (history.size() > 20) history = history.subList(0, 20);
        }
        save();
    }

    public void pin(String p) {
        if (!pinned.contains(p)) pinned.add(0, p);
        history.remove(p);
        save();
    }

    public void unpin(String p) {
        pinned.remove(p);
        save();
    }

    public void addFilter(String f) {
        if (!filters.contains(f)) { filters.add(f); save(); }
    }

    public void removeFilter(String f) {
        filters.remove(f);
        save();
    }
}
