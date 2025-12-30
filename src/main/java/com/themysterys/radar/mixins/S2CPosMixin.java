package com.themysterys.radar.mixins;

import com.themysterys.radar.utils.Utils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.world.scores.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPacketListener.class)
public class S2CPosMixin {

    @Inject(method = "handleSetDisplayObjective", at = @At("TAIL"))
    private void onSidebarChanged(ClientboundSetDisplayObjectivePacket packet, CallbackInfo ci){
        if (!Utils.isOnIsland()) return;

        if (packet.getSlot() != DisplaySlot.SIDEBAR) return;

        Utils.parseSidebar(packet);

    }
}
