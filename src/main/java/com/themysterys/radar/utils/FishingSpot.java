package com.themysterys.radar.utils;

import com.themysterys.radar.Radar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.DisplayEntity;

import java.util.List;
import java.util.UUID;

public class FishingSpot {
    private final String cords;
    private final List<String> perks;
    private final String island;
    private final DisplayEntity.TextDisplayEntity entity;

    public FishingSpot(String cords, List<String> perks, String island, DisplayEntity.TextDisplayEntity entity) {
        this.cords = cords;
        this.perks = perks;
        this.island = island;
        this.entity = entity;
    }

    public DisplayEntity.TextDisplayEntity getEntity() {
        return entity;
    }

    public String format() {
        UUID uuid = MinecraftClient.getInstance().player.getUuid();
        String username = MinecraftClient.getInstance().player.getName().getString();
        Boolean shareUser = Radar.getInstance().getConfig().shareUser;

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("\"cords\": \"").append(cords).append("\",\n");
        json.append("\"perks\": [");

        for (int i = 0; i < perks.size(); i++) {
            json.append("\"").append(perks.get(i)).append("\"");
            if (i < perks.size() - 1) {
                json.append(", ");
            }
        }

        json.append("],\n");
        json.append("\"island\": \"").append(island).append("\",\n");

        json.append("\"uuid\": \"").append(uuid).append("\",\n");
        json.append("\"username\": \"").append(username).append("\",\n");
        json.append("\"shareUser\": ").append(shareUser).append("\n");

        json.append("}");

        return json.toString();
    }
}