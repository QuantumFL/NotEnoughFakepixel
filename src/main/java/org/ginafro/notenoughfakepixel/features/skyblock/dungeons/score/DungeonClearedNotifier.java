package org.ginafro.notenoughfakepixel.features.skyblock.dungeons.score;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.DungeonManager;

public class DungeonClearedNotifier {
    private static boolean reminded = true;

    public static void reminder() {
        if (reminded) return;

        if (ScoreManager.getExplorationClearScore() == 60) {
            Minecraft.getMinecraft().ingameGUI.displayTitle(EnumChatFormatting.GOLD + "Dungeon cleared!", "", 2, 100, 2);
            Minecraft.getMinecraft().thePlayer.sendChatMessage("[NEF] Dungeon 100% cleared!");
            reminded = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (!Configuration.dungeonsClearedNotifier ||
                !DungeonManager.checkEssentials() ||
                e.phase == TickEvent.Phase.END ||
                Minecraft.getMinecraft().thePlayer == null ||
                Minecraft.getMinecraft().theWorld == null) return;
        if (ScoreManager.currentSeconds > 0 && ScoreManager.currentSeconds <= 8) {
            reminded = false;
        }
        reminder();
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (DungeonManager.checkEssentials()) {
            if (event.message.getUnformattedText().equals("[NPC] Mort: Good luck.")) {
                reminded = false;
            }
        }
    }
}
