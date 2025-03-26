package com.themysterys.radar.utils;

import com.themysterys.radar.Radar;
import com.themysterys.radar.RadarClient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Utils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Logger logger = LoggerFactory.getLogger("Radar");

    public static void log(String message) {
        logger.info("[Radar] {}", message);
    }

    public static void error(String message) {
        logger.error("[Radar] {}", message);
    }

    public static void sendMiniMessage(String message, boolean prefix, TagResolver replacements) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) {
            return;
        }

        if (prefix) {
            message = "[<gradient:#00c8ff:#006eff>Radar</gradient>]: " + message;
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

    public static void spawnPartials(MapStatus status, int count) {
        if (MinecraftClient.getInstance().player == null) return;
        FishingBobberEntity bobber = MinecraftClient.getInstance().player.fishHook;

        if (bobber == null) return;

        DustParticleEffect particleEffect;

        switch (status) {
            case SUCCESS -> particleEffect = new DustParticleEffect(new Vector3f(0,1,0), 1);
            case EXISTS -> particleEffect = new DustParticleEffect(new Vector3f(0,0,1), 1);
            case UNAUTHORISED -> particleEffect = new DustParticleEffect(new Vector3f(1,0.5f,0), 1);
            case FAILED -> particleEffect = new DustParticleEffect(new Vector3f(1,0,0), 1);
            case null, default -> {
                return;
            }
        }

        for (int i = 0; i <= count; i++) {
            double randomX = (Math.random() - 0.5);
            double randomZ = (Math.random() - 0.5);
            bobber.getWorld().addParticle(particleEffect,bobber.getX()+randomX,bobber.getY()+1,bobber.getZ()+randomZ,0.2,0,0.2);
        }
    }

    private static final List<Integer> allowedStatusCodes = List.of(200,201,400,401);

    public static void sendRequest(String path, String data) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Radar.getURL()+ "/" + path))
                .header("Content-Type", "application/json")
                .header("Authorization", RadarClient.getInstance().getSecret())
                .POST(HttpRequest.BodyPublishers.ofString(data, StandardCharsets.UTF_8))
                .build();

        // Using sendAsync to avoid blocking the main thread
        CompletableFuture<HttpResponse<String>> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // You can optionally handle the response here
        futureResponse.thenAccept(response -> {
            // Handle the response (for example, logging or processing the body)
            if (!allowedStatusCodes.contains(response.statusCode())) {
                Utils.log("Received status code: " + response.statusCode());
                Utils.log("Response received:" + response.body());
            }

            if (Objects.equals(path, "spots")) {
                if (response.statusCode() == 201) {
                    spawnPartials(MapStatus.SUCCESS, 5);
                } else if (response.statusCode() == 200) {
                    spawnPartials(MapStatus.EXISTS,5);
                } else if (response.statusCode() == 401) {
                    spawnPartials(MapStatus.UNAUTHORISED,5);
                }else {
                    spawnPartials(MapStatus.FAILED, 5);
                }
            }
        }).exceptionally(ex -> {
            if (Objects.equals(path, "spots")) {
                spawnPartials(MapStatus.FAILED, 5);
            }
            Utils.error(ex.getMessage());
            return null;
        });
    }

    public enum MapStatus {
        SUCCESS,
        EXISTS,
        UNAUTHORISED,
        FAILED,
    }
}
