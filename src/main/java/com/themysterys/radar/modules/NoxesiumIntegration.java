package com.themysterys.radar.modules;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.clientbound.ClientboundMccServerPacket;
import com.themysterys.radar.RadarClient;
import com.themysterys.radar.utils.Utils;

public class NoxesiumIntegration {
    public void init() {
        Utils.log("Initializing NoxesiumIntegration");

        NoxesiumPackets.CLIENT_MCC_SERVER.addListener(this, (any, packet, ctx) -> handlePacket(packet));

        Utils.log("NoxesiumIntegration has been initialized");
    }

    public void handlePacket(ClientboundMccServerPacket packet) {
        String subType = packet.subType();
        if (Utils.isOnFishingIsland(subType)) {
            RadarClient.getInstance().setIsland(subType);
        } else {
            RadarClient.getInstance().setIsland(null);
        }
    }
}
