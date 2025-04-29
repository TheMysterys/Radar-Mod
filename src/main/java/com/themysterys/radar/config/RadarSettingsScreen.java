package com.themysterys.radar.config;

import com.themysterys.radar.Radar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class RadarSettingsScreen extends Screen {

    private static final Component TITLE_TEXT = Component.translatable("radar.config.title");
    private final RadarConfig config = Radar.getInstance().getConfig();

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 61, 33);

    public RadarSettingsScreen(Screen _screen) {
        super(TITLE_TEXT);
    }

    @Override
    protected void init() {
        LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
        linearLayout.addChild(new StringWidget(TITLE_TEXT, this.font), LayoutSettings::alignHorizontallyCenter);
        GridLayout gridWidget = new GridLayout();
        gridWidget.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
        adder.addChild(CycleButton.onOffBuilder(config.enabled).withTooltip(value -> Tooltip.create(Component.translatable("radar.config.enabled.tooltip"))).create(Component.translatable("radar.config.enabled"), (button, value) -> {
            config.enabled = value;
            config.save();
        }));
        adder.addChild(CycleButton.onOffBuilder(config.shareUser).withTooltip(value -> Tooltip.create(Component.translatable("radar.config.shareUser.tooltip"))).create(Component.translatable("radar.config.shareUser"), (button, value) -> {
            config.shareUser = value;
            config.save();
        }));

        boolean isTheMysterys = Minecraft.getInstance().isLocalPlayer(UUID.fromString("4e832e0d-14b6-4f8f-ace2-280a9bf9dd98"));

        if (isTheMysterys) {
            adder.addChild(CycleButton.onOffBuilder(Radar.getInstance().getDevMode()).withTooltip(value -> Tooltip.create(Component.translatable("radar.config.devMode.tooltip"))).create(Component.translatable("radar.config.devMode"), ((button, value) -> Radar.getInstance().toggleDevMode())));
        }

        this.layout.addToContents(gridWidget);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.added();
    }

    @Override
    public void added() {
        this.layout.arrangeElements();
    }
}
