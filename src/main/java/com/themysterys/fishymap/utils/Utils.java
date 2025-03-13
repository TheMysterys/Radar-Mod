package com.themysterys.fishymap.utils;

import com.themysterys.fishymap.Fishymap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Utils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Logger logger = LoggerFactory.getLogger("FishyMap");

    public static void log(String message) {
        logger.info("[FishyMap] {}", message);
    }

    public static void error(String message) {
        logger.error("[FishyMap] {}", message);
    }

    public static void warn(String message) {
        logger.warn("[FishyMap] {}", message);
    }

    public static void sendMiniMessage(String message, boolean prefix, TagResolver replacements) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) {
            return;
        }

        if (prefix) {
            message = "[<gradient:#00c8ff:#006eff>FishyMap</gradient>]: " + message;
        }
        Component parsed;
        if (replacements != null) {
            parsed = miniMessage.deserialize(message, replacements);
        } else {
            parsed = miniMessage.deserialize(message);
        }
        player.sendMessage(parsed);

    }

    public static Boolean isOnIsland() {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) {
            return false;
        }

        return serverInfo.address.endsWith("mccisland.net") || serverInfo.address.endsWith("mccisland.com");
    }

    public static Boolean isOnFishingIsland(String islandName) {
        List<String> islandList = List.of("temperate_1", "temperate_2", "temperate_3", "tropical_1", "tropical_2", "tropical_3", "barren_1", "barren_2", "barren_3");

        return islandList.contains(islandName);
    }

    public static void sendRequest(String path, String data) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Fishymap.getURL() + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(data, StandardCharsets.UTF_8))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
