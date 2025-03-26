package com.themysterys.radar;

import com.themysterys.radar.modules.NoxesiumIntegration;
import com.themysterys.radar.utils.AuthUtils;
import com.themysterys.radar.utils.FishingSpot;
import com.themysterys.radar.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Arrays;
import java.util.List;

public class RadarClient implements ClientModInitializer {

    private static RadarClient instance;
    private String sharedSecret;
    private final int maxWaitTime = 5 * 20; // 5 seconds

    private boolean isOnIsland = false;
    private boolean isFishing = false;

    private FishingSpot currentFishingSpot = null;
    private String currentIsland = null;

    private int waitTime = 0;

    public static RadarClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        new NoxesiumIntegration().init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!Radar.getInstance().getConfig().enabled) return;
            if (!Utils.isOnIsland()) return;
            if (currentIsland == null) return;

            checkFishing();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!Utils.isOnIsland()) return;
            isOnIsland = true;
            sharedSecret = AuthUtils.generateSharedSecret();

            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            if (player == null) {
                throw new IllegalStateException("Player is null. How are you joining a server...");
            }

            Utils.sendRequest("register", "{\"uuid\":\""+player.getUuid()+"\"}");

            if (Radar.getInstance().isNewInstallation) {
                Utils.sendMiniMessage("Thank you for installing Radar. Sharing your username is <yellow>disabled by default</yellow> and can be changed in the configuration menu. To access the configuration menu, press <bold><yellow>F3 + F</yellow></bold>. Happy Fishing", true, null);
                Radar.getInstance().isNewInstallation = false;
                Radar.getInstance().getConfig().save();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!isOnIsland) return;
            isOnIsland = false;
            setIsland(null);
            Utils.sendRequest("unregister", "");
            sharedSecret = null;
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (sharedSecret == null) return;
            Utils.sendRequest("unregister", "");
        });

        Utils.log("Radar has been initialized.");
    }

    private void checkFishing() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        assert player != null;


        FishingBobberEntity fishHook = player.fishHook;

        if (fishHook == null) {
            if (isFishing) {
                isFishing = false;
                return;
            }
            if (currentFishingSpot == null) {
                return;
            }
            if (waitTime == maxWaitTime) {
                resetFishingSpot();
                return;
            }
            waitTime++;
            return;
        }

        if (fishHook.isInFluid() && !isFishing) {
            isFishing = true;
            waitTime = 0;
            getFishingSpot(player, fishHook);
        }
    }

    private void getFishingSpot(ClientPlayerEntity player, FishingBobberEntity fishHook) {

        BlockPos blockPos = fishHook.getBlockPos();
        Box box = Box.of(blockPos.toCenterPos(), 3.5, 6.0, 3.5);
        List<Entity> entities = player.getWorld().getOtherEntities(null, box).stream().filter(entity -> entity instanceof DisplayEntity.TextDisplayEntity).toList();

        if (!entities.isEmpty()) {
            DisplayEntity.TextDisplayEntity textDisplay = (DisplayEntity.TextDisplayEntity) entities.getFirst();
            if (currentFishingSpot != null && currentFishingSpot.getEntity().equals(textDisplay)) {
                return;
            }

            String text = textDisplay.getText().asTruncatedString(Integer.MAX_VALUE);

            int fishingSpotX = textDisplay.getBlockX();
            int fishingSpotZ = textDisplay.getBlockZ();

            List<String> perks = Arrays.stream(text.split("\n")).filter(line -> line.contains("+")).map(line -> "+" + line.split("\\+")[1]).toList();
            if (!perks.isEmpty()) {

                currentFishingSpot = new FishingSpot(fishingSpotX + "/" + fishingSpotZ, perks, currentIsland, textDisplay);

                Utils.sendRequest("spots", currentFishingSpot.format());

                return;
            }
        }
        Utils.spawnPartials(Utils.MapStatus.FAILED, 5);
    }

    private void resetFishingSpot() {
        if (currentFishingSpot == null) return;

        currentFishingSpot = null;
    }

    public void setIsland(String island) {
        if (island == null) {
            isFishing = false;
            resetFishingSpot();
        }
        currentIsland = island;
    }

    public String getSecret() {
        return sharedSecret;
    }
}
