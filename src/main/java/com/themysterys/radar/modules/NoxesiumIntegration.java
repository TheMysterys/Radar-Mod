package com.themysterys.radar.modules;

import com.noxcrew.noxesium.core.fabric.mcc.MccNoxesiumEntrypoint;
import com.noxcrew.noxesium.core.mcc.MccPackets;
import com.noxcrew.noxesium.core.mcc.ClientboundMccServerPacket;
import com.themysterys.radar.RadarClient;
import com.themysterys.radar.utils.Utils;

import java.util.List;

public class NoxesiumIntegration {
    public void init() {
        Utils.log("Initializing NoxesiumIntegration");


        MccPackets.CLIENTBOUND_MCC_SERVER.addListener(this, ClientboundMccServerPacket.class,(any, packet, ctx) -> handlePacket(packet));

        Utils.log("NoxesiumIntegration has been initialized");
    }

    public void handlePacket(ClientboundMccServerPacket packet) {
        List<String> types = packet.types();
        String subType = types.getLast();
        if (Utils.isOnFishingIsland(subType)) {
            RadarClient.getInstance().setIsland(subType);
        } else {
            RadarClient.getInstance().setIsland(null);
        }
    }
}
