package org.ginafro.notenoughfakepixel.features.skyblock.slayers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;
import org.ginafro.notenoughfakepixel.variables.Gamemode;

import java.util.*;

public class MinibossAlert {
    private static final Set<String> MINIBOSS = new HashSet<>(Arrays.asList(
        "Revenant Sycophant", "Revenant Champion", "Deformed Revenant", "Atoned Champion", "Atoned Revenant", // Zombie
        "Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula", // Spider
        "Pack Enforcer", "Sven Follower", "Sven Alpha", // Wolf
        "Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac", // Enderman
        "Flare Demon", "Kindleheart Demon", "Burningsoul Demon" // Blaze
    ));

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set<UUID> detectedMinibosses = new HashSet<>(); // track detected minibosses

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isSkyblock()) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity.getUniqueID() != null && detectedMinibosses.contains(entity.getUniqueID())) continue;

            String entityName = entity.getDisplayName().getUnformattedText();
            for (String keyword : MINIBOSS) {
                if (entityName.contains(keyword)) {
                    detectedMinibosses.add(entity.getUniqueID());
                    triggerAlerts();
                    break;
                }
            }
        }
    }

    private boolean isSkyblock() {
        return ScoreboardUtils.currentGamemode == Gamemode.SKYBLOCK;
    }

    private void triggerAlerts() {
        if (Configuration.slayerMinibossTitle) {
            mc.ingameGUI.displayTitle(EnumChatFormatting.RED + "MiniBoss", "", 0, 10, 0); // Display title for 10 ticks
            // Display empty title after 5 ticks to clear the previous title
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mc.ingameGUI.displayTitle("", "", 0, 5, 0); // Display empty title
                }
            }, 1000);
        }
        if (Configuration.slayerMinibossSound) {
            playSoundWithDelay(5, 70);
        }
    }

    private void playSoundWithDelay(int times, int delay) {
        new Thread(() -> {
            try {
                for (int i = 0; i < times; i++) {
                    playSound();
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void playSound() {
        if (mc.theWorld != null) {
            mc.theWorld.playSound(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                "random.orb",
                1.0F,
                1.0F,
                false
            );
        }
    }
}
