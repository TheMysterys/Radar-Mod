package com.themysterys.radar.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.themysterys.radar.config.RadarSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(Keyboard.class)
public class CustomDebugHotkeysMixin {


    @WrapOperation(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V"))
    public void addRadarHelpMessage(ChatHud instance, Text message, Operation<Void> original) {
        if (message.getContent() instanceof TranslatableTextContent translatableContents) {
            if (translatableContents.getKey().equals("debug.pause.help")) {
                instance.addMessage(Text.translatable("debug.radar.help"));
            }
        }
        original.call(instance, message);
    }

    @ModifyReturnValue(method = "processF3", at = @At("TAIL"))
    public boolean toggleExperimentalPatches(boolean original, int keyCode) {
        if (keyCode == InputUtil.GLFW_KEY_F) {
            MinecraftClient.getInstance().setScreen(new RadarSettingsScreen(null));
            return true;
        }
        return original;
    }
}
