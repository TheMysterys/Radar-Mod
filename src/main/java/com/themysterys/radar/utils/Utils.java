package com.themysterys.radar.utils;

import com.themysterys.radar.Radar;
import com.themysterys.radar.RadarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger("Radar");
    private static final List<Integer> allowedStatusCodes = List.of(200, 201, 400, 401);

    public static Map<String,String> islandList = Map.of(
            "verdant_woods", "temperate_1",
            "floral_forest", "temperate_2",
            "dark_grove", "temperate_3",
            "tropical_overgrowth", "tropical_1",
            "coral_shores", "tropical_2",
            "twisted_swamp", "tropical_3",
            "ancient_sands", "barren_1",
            "blazing_canyon", "barren_2",
            "ashen_wastes", "barren_3"
    );

    public static Style mccFont() {
        return Style.EMPTY.withFont(new FontDescription.Resource(ResourceLocation.fromNamespaceAndPath("mcc","icon")));
    }

    private static MutableComponent getPrefix() {
        return Component.literal("[")
                .append(Component.literal("Radar").withColor(TextColor.parseColor("#006eff").getOrThrow().getValue()))
                .append("]: ");
    }

    public static void log(String message) {
        logger.info("[Radar] {}", message);
    }

    public static void error(String message) {
        logger.error("[Radar] {}", message);
    }

    public static void sendMessage(Component message, boolean prefix) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }
        Component component;

        if (prefix) {
            component = getPrefix().append(message);
        } else {
            component = message;
        }

        player.displayClientMessage(component, false);
    }

    public static Boolean isOnIsland() {
        ServerData serverInfo = Minecraft.getInstance().getCurrentServer();
        if (serverInfo == null) {
            return false;
        }

        return serverInfo.ip.endsWith("mccisland.net") || serverInfo.ip.endsWith("mccisland.com");
    }

    public static Boolean isOnFishingIsland(String islandName) {
        return islandList.containsKey(islandName);
    }

    public static void parseSidebar(ClientboundSetDisplayObjectivePacket packet){
        Minecraft client = Minecraft.getInstance();

        if (client.player == null) return;
        if (client.level == null) return;

        String objectiveName;
        Scoreboard scoreboard = client.level.getScoreboard();

        if (packet != null) {
            objectiveName = packet.getObjectiveName();
        }
        else {
            objectiveName = Objects.requireNonNull(scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)).getName();
        }

        Objective objective = scoreboard.getObjective(objectiveName);

        if (objective == null) return;

        String locationName = objective.getDisplayName().getString().toLowerCase().replace("mcci: ", "").replace(" ", "_");

        if (Utils.isOnFishingIsland(locationName)) {
            RadarClient.getInstance().setIsland(locationName);
        } else {
            RadarClient.getInstance().setIsland(null);
        }
    }

    public static void spawnPartials(MapStatus status, int count) {
        if (Minecraft.getInstance().player == null) return;
        LocalPlayer player = Minecraft.getInstance().player;
        FishingHook bobber = player.fishing;

        if (bobber == null) {
            return;
        }

        DustParticleOptions particleEffect;

        switch (status) {
            case SUCCESS -> {
                particleEffect = new DustParticleOptions(ARGB.color(0, 255, 0), 1);
                if (Radar.getInstance().getConfig().playSound) {
                    Minecraft.getInstance().schedule(() -> player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1, 1));
                }
            }
            case EXISTS -> {
                particleEffect = new DustParticleOptions(ARGB.color(0, 0, 255), 1);
                if (Radar.getInstance().getConfig().playSound) {
                    Minecraft.getInstance().schedule(() -> player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1, 0.5f));
                }
            }
            case UNAUTHORISED -> particleEffect = new DustParticleOptions(ARGB.colorFromFloat(1, 1, 0.5f, 0), 1);
            case FAILED -> particleEffect = new DustParticleOptions(ARGB.color(255, 0, 0), 1);
            case null, default -> {
                return;
            }
        }
        SecureRandom random = new SecureRandom();
        for (int i = 0; i <= count; i++) {
            double randomX = (random.nextDouble() - 0.5);
            double randomZ = (random.nextDouble() - 0.5);

            bobber.level().addParticle(particleEffect, bobber.getX() + randomX, bobber.getY() + 1, bobber.getZ() + randomZ, 0.2, 0, 0.2);
        }
    }

    public static void sendRequest(String path, String data) {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Radar.getURL() + "/" + path))
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
                Utils.log("Request: /"+ path);
                Utils.log("Received status code: " + response.statusCode());
                Utils.log("Response received:" + response.body());
            }

            if (Objects.equals(path, "spots")) {
                if (response.statusCode() == 201) {
                    spawnPartials(MapStatus.SUCCESS, 5);
                } else if (response.statusCode() == 200) {
                    spawnPartials(MapStatus.EXISTS, 5);
                } else if (response.statusCode() == 401) {
                    spawnPartials(MapStatus.UNAUTHORISED, 5);
                } else {
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
        SUCCESS, EXISTS, UNAUTHORISED, FAILED,
    }
}
