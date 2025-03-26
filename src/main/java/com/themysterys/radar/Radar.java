package com.themysterys.radar;

import com.themysterys.radar.config.RadarConfig;
import com.themysterys.radar.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;

public class Radar implements ModInitializer {

    public boolean isNewInstallation = !Files.exists(FabricLoader.getInstance().getConfigDir().resolve("radar.json"));
    private final RadarConfig config = RadarConfig.load();

    private static final String url = "http://localhost:8879"; //"https://fishyapi.themysterys.com";

    private static Radar instance;

    @Override
    public void onInitialize() {
        Utils.log("Radar is initializing!");
        instance = this;
    }

    public RadarConfig getConfig() {
        return config;
    }

    public static Radar getInstance() {
        return instance;
    }

    public static String getURL() {
        return url;
    }
}
