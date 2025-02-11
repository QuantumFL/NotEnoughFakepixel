package org.ginafro.notenoughfakepixel.features.skyblock.dungeons.score;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.DungeonManager;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;
import org.ginafro.notenoughfakepixel.utils.TablistParser;
import org.ginafro.notenoughfakepixel.variables.DungeonFloor;

public class ScoreManager {
    static int failedPuzzles = 0;
    public static int currentSeconds = -1;
    private static int reducedPenalty = 0;

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!DungeonManager.checkEssentials()) return;
        if (event.message.getUnformattedText().contains("PUZZLE FAIL!")) {
            failedPuzzles++;
        } else if (event.message.getUnformattedText().equals("Your Spirit Pet reduced your death score penalty to 1!")) {
            reducedPenalty = 1;
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc [NEF] I died with Spirit Pet, so my kill doesn't count");
        }  else if (event.message.getUnformattedText().contains("[NEF] I died with Spirit Pet, so my kill doesn't count")) {
            reducedPenalty = 1;
        }
    }

    @SubscribeEvent()
    public void onWorldLoad(WorldEvent.Load event) {
        if (!DungeonManager.checkEssentials()) return;
        reset();
    }

    private void reset() {
        failedPuzzles = 0;
        currentSeconds = -1;
        reducedPenalty = 0;
    }

    public static int getTotalScore() {
        return getSkillScore() + getExplorationScore() + getSpeedScore() + getBonusScore();
    }

    public static int getSkillScore() {
        int deaths = TablistParser.deaths;
        return Math.max(Math.min(100 - (deaths-reducedPenalty) * 2 - failedPuzzles * 14, 100), 0);
    }

    public static int getExplorationClearScore() {
        int clearedPercentage = ScoreboardUtils.clearedPercentage;
        return (int) Math.max(Math.min(Math.floor(60f * clearedPercentage / 100f),60),0);
    }

    public static int getExplorationSecretScore() {
        int secretPercentage = getSecretPercentage();
        int secretNeeded = DungeonFloor.getFloor(ScoreboardUtils.currentFloor.name()).getSecretPercentage();
        return (int) Math.max(Math.min(Math.floor(40f * secretPercentage / secretNeeded), 40f),0);
    }

    public static int getExplorationScore() {
        return getExplorationClearScore() + getExplorationSecretScore();
    }

    public static int getSpeedScore() {
        String currentTimeString = TablistParser.time;
        currentSeconds = convertToSeconds(currentTimeString);
        if (currentSeconds == -1) {
            return 100;
        }
        int t = currentSeconds + DungeonFloor.getFloor(ScoreboardUtils.currentFloor.name()).getT();
        if (t < 480) return 100;
        if (t < 600) return 140 - (int) Math.ceil(t * (1f / 12f));
        if (t < 840) return 115 - (int) Math.ceil(t * (1f / 24f));
        if (t < 1140) return 108 - (int) Math.ceil(t * (1f / 30f));
        if (t < 3940) return (int) Math.ceil(98.5f - (int) Math.ceil(t * (1f / 40f)));
        return 0;
    }

    public static int getBonusScore() {
        int crypts = TablistParser.crypts;
        return (Configuration.dungeonsIsPaul ? 10 : 0) + Math.min(5, crypts);
    }

    public static int getSecretPercentage() {
        return TablistParser.secretPercentage;
    }

    public static int getRequiredSecretNeeded() {
        int secretScoreNeeded = 300-60-getSkillScore()-getSpeedScore()-getBonusScore();
        if (secretScoreNeeded > 40) {
            // cannot reach
            return -1;
        }
        return (int) Math.ceil(secretScoreNeeded * DungeonFloor.getFloor(ScoreboardUtils.currentFloor.name()).getSecretPercentage() / 40f);
    }

    public static int convertToSeconds(String time) {
        if (time.isEmpty()) return -1;
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return (minutes * 60) + seconds;
    }
}
