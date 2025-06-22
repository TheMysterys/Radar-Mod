package com.themysterys.radar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.themysterys.radar.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RadarConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public boolean shareUser = false;
    public boolean playSound = true;

    public static RadarConfig load() {
        var file = getConfigFile();
        if (Files.exists(file)) {
            try (var reader = new FileReader(file.toFile())) {
                return GSON.fromJson(reader, RadarConfig.class);
            } catch (Exception x) {
                Utils.error("Failed to load config file: " + x.getMessage());
            }
        }
        return new RadarConfig();
    }

    public void save() {
        try {
            Files.writeString(getConfigFile(), GSON.toJson(this));
        } catch (Exception x) {
            Utils.error("Failed to save config file: " + x.getMessage());
        }
    }

    public static Path getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("radar.json");
    }
}
