package com.themysterys.fishymap;

import com.themysterys.fishymap.config.FishymapConfig;
import com.themysterys.fishymap.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;

public class Fishymap implements ModInitializer {

    public boolean isNewInstallation = !Files.exists(FabricLoader.getInstance().getConfigDir().resolve("fishymap.json"));
    private final FishymapConfig config = FishymapConfig.load();

    private static final String url = "http://localhost:5173";

    private static Fishymap instance;

    @Override
    public void onInitialize() {
        Utils.log("Fishymap is initializing!");
        instance = this;
    }

    public FishymapConfig getConfig() {
        return config;
    }

    public static Fishymap getInstance() {
        return instance;
    }

    public static String getURL() {
        return url;
    }
}
