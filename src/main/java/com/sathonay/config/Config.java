package com.sathonay.config;

import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public class Config extends YamlConfiguration {

    @Getter
    private Plugin plugin;
    @Getter
    private String filePath;
    @Getter
    private File file;

    public Config(Plugin plugin, String filePath) {
        this(filePath);
        this.plugin = plugin;
    }

    public Config(Plugin plugin, File file) {
        this(file);
        this.plugin = plugin;
    }

    public Config(String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    public Config(File file) {
        this.filePath = file.getPath();
        this.file = file;
    }

    public Config load() {
        load(false);
        return this;
    }

    public Config saveDefault() {
        return saveDefault(false);
    }

    public Config saveDefault(boolean replace) {
        saveDefault(replace, false);
        return this;
    }

    public CompletableFuture<Config> saveDefault(boolean replace, boolean async) {
        return saveDefault(this.plugin, replace, async);
    }

    public CompletableFuture<Config> saveDefault(Plugin plugin, boolean replace, boolean async) {
        return buildCompletableFuture(() -> saveDefaultConfig(plugin, replace), async);
    }

    private Config saveDefaultConfig(Plugin plugin, boolean replace) {

        if (plugin != null) {
            plugin.saveResource(this.filePath, replace);
            setFile(plugin.getDataFolder() + "/" + filePath);
        }

        return this;
    }

    public CompletableFuture<Config> load(boolean async) {
        return buildCompletableFuture(this::loadConfig, async);
    }

    private Config loadConfig() {

        if (file.exists()) {
            try {
                this.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        
        return this;
    }

    public Config save() {
        save(false);
        return this;
    }

    public CompletableFuture<Config> save(boolean async) {
        return buildCompletableFuture(this::saveConfig, async);
    }

    private Config saveConfig() {
        
        try {
            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public Config clearConfig() {
        this.map.clear();
        return this;
    }

    public Map<String, Object> getConfigCache() {
        return this.map;
    }

    public boolean exist() {
        return file.exists();
    }

    public Config setFile(File file) {
        this.file = file;
        this.filePath = file.getPath();
        return this;
    }

    public Config setFile(String filePath) {
        this.setFile(new File(filePath));
        return this;
    }

    private CompletableFuture<Config> buildCompletableFuture(Callable<Config> callable, boolean async) {

        if (async) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return callable.call();
                } catch (RuntimeException exception) {
                    throw exception;
                } catch (Exception exception) {
                    throw new CompletionException(exception);
                }
            });
        }

        return CompletableFuture.completedFuture(callable.call());
    }
}
