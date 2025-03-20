package com.themysterys.fishymap.config;

import com.themysterys.fishymap.Fishymap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class FishymapSettingsScreen extends Screen {

    private static final Text TITLE_TEXT = Text.translatable("fishymap.config.title");
    private final FishymapConfig config = Fishymap.getInstance().getConfig();

    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 61, 33);

    public FishymapSettingsScreen(Screen _screen) {
        super(TITLE_TEXT);
    }

    @Override
    protected void init() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(8));
        directionalLayoutWidget.add(new TextWidget(TITLE_TEXT, this.textRenderer), Positioner::alignHorizontalCenter);
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(CyclingButtonWidget.onOffBuilder(config.enabled)
                .tooltip(value -> Tooltip.of(Text.translatable("fishymap.config.enabled.tooltip")))
                .build(
                        Text.translatable("fishymap.config.enabled"), (button, value) -> {
                            config.enabled = value;
                            config.save();
                        }
                ));
        adder.add(CyclingButtonWidget.onOffBuilder(config.shareUser)
                .tooltip(value -> Tooltip.of(Text.translatable("fishymap.config.shareUser.tooltip")))
                .build(
                Text.translatable("fishymap.config.shareUser"), (button, value) -> {
                    config.shareUser = value;
                    config.save();
                }
        ));

        this.layout.addBody(gridWidget);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build());
        this.layout.forEachChild(element -> {
            ClickableWidget var10000 = this.addDrawableChild(element);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }
}
