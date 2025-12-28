package com.themysterys.radar.mixins;

import com.themysterys.radar.RadarClient;
import com.themysterys.radar.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
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

        Minecraft client = Minecraft.getInstance();

        if (client.player == null) return;
        if (client.level == null) return;

        String objectiveName = packet.getObjectiveName();
        Scoreboard scoreboard = client.level.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) return;

        String locationName = objective.getDisplayName().getString().toLowerCase().replace("mcci: ", "").replace(" ", "_");

        if (Utils.isOnFishingIsland(locationName)) {
            RadarClient.getInstance().setIsland(locationName);
        } else {
            RadarClient.getInstance().setIsland(null);
        }
    }
}
