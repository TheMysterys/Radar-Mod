package com.themysterys.radar.config;

import com.themysterys.radar.Radar;
import com.themysterys.radar.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class RadarSettingsScreen extends OptionsSubScreen {

    private static final Component TITLE_TEXT = Component.translatable("radar.config.title");
    private final RadarConfig config = Radar.getInstance().getConfig();

    public RadarSettingsScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, TITLE_TEXT);
    }

    OptionInstance<Boolean> modEnabled = OptionInstance.createBoolean("radar.config.enabled", _ -> Tooltip.create(Component.translatable("radar.config.enabled.tooltip")), config.enabled, val -> {
        config.enabled = val;
        config.save();
    });

    OptionInstance<Boolean> shareUser = OptionInstance.createBoolean("radar.config.shareUser", _ -> Tooltip.create(Component.translatable("radar.config.shareUser.tooltip")), config.shareUser, val -> {
        config.shareUser = val;
        config.save();
    });

    OptionInstance<Boolean> playSound = OptionInstance.createBoolean("radar.config.playSound", _ -> Tooltip.create(Component.translatable("radar.config.playSound.tooltip")), config.playSound, val -> {
        config.playSound = val;
        config.save();
    });

    OptionInstance<Boolean> debugLogs = OptionInstance.createBoolean("radar.config.debugLogs", _ -> Tooltip.create(Component.translatable("radar.config.debugLogs.tooltip")), config.debugLogs, val -> {
        config.debugLogs = val;
        config.save();
    });

    OptionInstance<Boolean> devMode = OptionInstance.createBoolean("radar.config.devMode", _ -> Tooltip.create(Component.translatable("radar.config.devMode.tooltip")), Radar.getInstance().getDevMode(), _ -> Radar.getInstance().toggleDevMode());

    @Override
    protected void addOptions() {
        boolean isTheMysterys = Minecraft.getInstance().isLocalPlayer(UUID.fromString("4e832e0d-14b6-4f8f-ace2-280a9bf9dd98"));
        if (this.list == null) {
            Utils.log("Error: List is null when trying to make settings screen.");
            return;
        }
        this.list.addSmall(modEnabled, shareUser);
        this.list.addSmall(playSound, debugLogs);
        if (isTheMysterys) {
            this.list.addBig(devMode);
        }
    }
}
