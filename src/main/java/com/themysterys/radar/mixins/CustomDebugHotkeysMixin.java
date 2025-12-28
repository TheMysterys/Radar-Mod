package com.themysterys.radar.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.themysterys.radar.config.RadarSettingsScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(KeyboardHandler.class)
public class CustomDebugHotkeysMixin {


    @WrapOperation(method = "showDebugChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    public void addRadarHelpMessage(ChatComponent instance, Component message, Operation<Void> original) {
        if (message.getContents() instanceof TranslatableContents translatableContents) {
            if (translatableContents.getKey().equals("debug.chunk_boundaries.help")) {
                instance.addMessage(Component.translatable("debug.radar.help"));
            }
        }
        original.call(instance, message);
    }

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    public void toggleExperimentalPatches(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (keyEvent.key() == InputConstants.KEY_F) {
            Minecraft.getInstance().setScreen(new RadarSettingsScreen(null));
            cir.setReturnValue(true);
        }
    }
}
