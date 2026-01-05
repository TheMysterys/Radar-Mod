package com.themysterys.radar.modules;

import com.themysterys.radar.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class AutoRod {
    private static int index = 0;

    private static final MutableComponent[] components = new MutableComponent[] {
            Component.literal("Oops did I leave this command in the mod "),
            Component.literal("Oh well it doesn't do anything I promise "),
            Component.literal("No really, you have to believe me "),
            Component.literal("Trust me, this command is useless "),
            Component.literal("No one ever trusts me anymore "),
            Component.literal("Look this is a mapping mod. Not an auto fishing mod"),
            Component.literal("Why would I even put auto fishing in here. I'm a ").append(Component.literal("Moderator").withColor(11613948)),
            Component.literal("Look just stop trying, I'm warning you."),
            Component.literal("Do you ").append(Component.literal("really").withStyle(ChatFormatting.ITALIC)).append(" want to break the server rules "),
            Component.literal("..."),
            Component.literal("Look if you keep running this, I'll have to report you to the ").append(Component.literal("Noxcrew").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)),
            Component.literal("This is your last chance..."),
            Component.literal("You have now been reported to the ").append(Component.literal("Noxcrew ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)),
    };

    public static void sendMessage() {
        if (index < components.length) {
            Component message = components[index];
            index++;
            Utils.sendMessage(message, true);
        }
    }
}
