package com.themysterys.fishymap;

import com.themysterys.fishymap.modules.NoxesiumIntegration;
import com.themysterys.fishymap.utils.AuthUtils;
import com.themysterys.fishymap.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
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
import java.util.stream.Collectors;

public class FishymapClient implements ClientModInitializer {

    private static FishymapClient instance;
    private final String sharedSecret = AuthUtils.generateSharedSecret();

    private boolean isOnIsland = false;
    private boolean isFishing = false;
    private DisplayEntity.TextDisplayEntity currentFishingSpot = null;

    private String currentIsland = null;

    private int waitTime = 0;
    private final int maxWaitTime = 5 * 20; // 5 seconds


    @Override
    public void onInitializeClient() {
        instance = this;
        new NoxesiumIntegration().init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!Utils.isOnIsland()) return;
            if (currentIsland == null) return;

            checkFishing();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!Utils.isOnIsland()) return;
            isOnIsland = true;

            if (Fishymap.getInstance().isNewInstallation) {
                Utils.sendMiniMessage("Thank you for installing FishyMap. To access the configuration menu, press <bold>F3 + F</bold>. Happy Fishing", true, null);
                Fishymap.getInstance().isNewInstallation = false;
                Fishymap.getInstance().getConfig().save();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!isOnIsland) return;
            isOnIsland = false;
            setIsland(null);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("fishymap").executes(context -> {
            Utils.sendMiniMessage("Island: " + currentIsland + "\nShared Secret:" + sharedSecret, true, null);
            return 1;
        })));

        Utils.log("FishyMap has been initialized.");
    }

    private void checkFishing() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        assert player != null;


        FishingBobberEntity fishHook = player.fishHook;

        if (fishHook == null) {
            if (isFishing) {
                isFishing = false;
                Utils.log("Stopped Fishing");
                return;
            }
            if (currentFishingSpot == null) {
                return;
            }
            if (waitTime == maxWaitTime) {
                Utils.log("Resetting currentFishingSpot");
                resetFishingSpot();
                return;
            }
            waitTime++;
            return;
        }

        if (fishHook.isInFluid() && !isFishing) {
            isFishing = true;
            waitTime = 0;
            Utils.log("Started Fishing");
            getFishingSpot(player, fishHook);
        }
    }

    private void getFishingSpot(ClientPlayerEntity player, FishingBobberEntity fishHook) {

        BlockPos blockPos = fishHook.getBlockPos();
        Box box = Box.of(blockPos.toCenterPos(), 3.0, 6.0, 3.0);
        List<Entity> entities = player.getWorld().getOtherEntities(null, box).stream().filter(entity -> entity instanceof DisplayEntity.TextDisplayEntity).toList();

        if (!entities.isEmpty()) {
            DisplayEntity.TextDisplayEntity textDisplay = (DisplayEntity.TextDisplayEntity) entities.getFirst();
            if (currentFishingSpot != null && currentFishingSpot.equals(textDisplay)) {
                Utils.log("Already found this fishing spot.");
                return;
            }
            String text = textDisplay.getText().asTruncatedString(Integer.MAX_VALUE);

            int fishingSpotX = textDisplay.getBlockX();
            int fishingSpotZ = textDisplay.getBlockZ();

            String perks = Arrays.stream(text.split("\n"))
                    .filter(line -> line.contains("+"))
                    .map(line -> "+" + line.split("\\+")[1])
                    .collect(Collectors.joining(", "));
            if (!perks.isEmpty()) {
                Utils.log("Fishing spot X/Z: " + fishingSpotX + "/" + fishingSpotZ);
                Utils.log("Fishing spot perks: " + perks);
                currentFishingSpot = textDisplay;

                // TODO: Code that updates the map

                return;
            }
        }
        Utils.error("Could not find a fishing spot.");
        isFishing = false;
    }

    private void resetFishingSpot() {
        currentFishingSpot = null;

        // Post request to website
        Utils.sendRequest("removespot", "");


    }

    public void setIsland(String island) {
        Utils.log("Setting island to " + island);
        if (island == null) {
            isFishing = false;
            resetFishingSpot();
        }
        currentIsland = island;
    }

    public static FishymapClient getInstance() {
        return instance;
    }
}
