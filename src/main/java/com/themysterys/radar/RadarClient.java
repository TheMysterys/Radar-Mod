package com.themysterys.radar;

import com.themysterys.radar.config.RadarSettingsScreen;
import com.themysterys.radar.modules.AutoRod;
import com.themysterys.radar.utils.AuthUtils;
import com.themysterys.radar.utils.FishingSpot;
import com.themysterys.radar.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.List;

public class RadarClient implements ClientModInitializer {

    private static RadarClient instance;
    private String sharedSecret;

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

            Player player = Minecraft.getInstance().player;

            if (player == null) {
                throw new IllegalStateException("Player is null. How are you joining a server...");
            }

            Utils.sendRequest("register", "{\"uuid\":\"" + player.getUUID() + "\"}");

            if (Radar.getInstance().isNewInstallation) {
                MutableComponent[] components = new MutableComponent[] {
                        Component.literal("Thank you for installing Radar."),
                        Component.literal("Sharing your username is ").append(Component.literal("disabled by default ").withColor(16777045)).append("and can be"),
                        Component.literal("changed in the configuration menu."),
                        Component.literal("To access the configuration menu, press ").append(Component.literal("F3 + F").withStyle(ChatFormatting.BOLD,ChatFormatting.YELLOW)).append("."),
                        Component.literal("Happy Fishing")
                };
                MutableComponent result = null;
                for (MutableComponent component : components) {
                    if (result == null) {
                        result = component;
                    } else{
                        result.append("\n").append(component);
                    }
                }

                Utils.sendMessage(result, true);
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


        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("radar").then(ClientCommandManager.literal("settings").executes(context -> {
            Minecraft.getInstance().schedule(() -> Minecraft.getInstance().setScreen(new RadarSettingsScreen((null))));
            return 1;
        })).then(ClientCommandManager.literal("colors").executes(context -> {
            MutableComponent[] components = new MutableComponent[] {
                    Component.literal("Radar particle colors:"),
                    Component.literal("Green").withStyle(ChatFormatting.GREEN).append(Component.literal(": Successfully added to map").withStyle(ChatFormatting.WHITE)),
                    Component.literal("Blue").withStyle(ChatFormatting.BLUE).append(Component.literal(": Spot already added to map").withStyle(ChatFormatting.WHITE)),
                    Component.literal("Orange").withColor(16744192).append(Component.literal(": Unauthorized. Rejoin server to reauthenticate").withStyle(ChatFormatting.WHITE)),
                    Component.literal("Red").withStyle(ChatFormatting.RED).append(Component.literal(": There was an error. Please try again").withStyle(ChatFormatting.WHITE))
            };
            MutableComponent result = null;
            for (MutableComponent component : components) {
                if (result == null) {
                    result = component;
                } else{
                    result.append("\n").append(component);
                }
            }
            Utils.sendMessage(result, true);
            return 1;
        })).then(ClientCommandManager.literal("map").executes(context -> {
            Minecraft.getInstance().schedule(() -> Util.getPlatform().openUri("https://radar.themysterys.com/"));
            return 1;
        })).then(ClientCommandManager.literal("autorod").executes(context -> {
            if (!isOnIsland) return 1;
            AutoRod.sendMessage();
            return 1;
        })).executes(context -> {
            Utils.sendMessage(
                    Component.literal("Available commands: ")
                            .append(Component.literal("colors").withStyle(ChatFormatting.YELLOW))
                            .append(", ")
                            .append(Component.literal("settings").withStyle(ChatFormatting.YELLOW))
                            .append(", ")
                            .append(Component.literal("map").withStyle(ChatFormatting.YELLOW)),
                    true);
            return 1;
        })));

        Utils.log("Radar has been initialized.");
    }

    private void checkFishing() {
        Player player = Minecraft.getInstance().player;

        assert player != null;

        FishingHook fishHook = player.fishing;

        if (fishHook == null) {
            if (isFishing) {
                isFishing = false;
                return;
            }
            if (currentFishingSpot == null) {
                return;
            }
            // 5 seconds
            int maxWaitTime = 5 * 20;
            if (waitTime == maxWaitTime) {
                resetFishingSpot();
                return;
            }
            waitTime++;
            return;
        }

        if (fishHook.isInLiquid() && !isFishing) {
            isFishing = true;
            waitTime = 0;
            getFishingSpot(player, fishHook);
        }
    }

    private void getFishingSpot(Player player, FishingHook fishHook) {
        Utils.parseSidebar(null);

        BlockPos blockPos = fishHook.getOnPos();
        AABB box = AABB.ofSize(blockPos.getCenter(), 3.5, 6.0, 3.5);
        List<Entity> entities = player.level().getEntities(null, box).stream().filter(entity -> entity instanceof Display.TextDisplay).toList();

        if (!entities.isEmpty()) {
            Display.TextDisplay textDisplay = (Display.TextDisplay) entities.getFirst();
            if (currentFishingSpot != null && currentFishingSpot.getEntity().equals(textDisplay)) {
                return;
            }

            String text = textDisplay.getText().getString(Integer.MAX_VALUE);

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
        else {
            island = Utils.islandList.get(island);
        }
        currentIsland = island;
    }

    public String getSecret() {
        return sharedSecret;
    }
}
