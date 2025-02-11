package org.ginafro.notenoughfakepixel.utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SoundUtils {

    public static void playSound(int[] cords, String sound, float volume, float pitch) {
        playSound(cords, sound, volume, pitch, 1);
    }

    public static void playSound(int[] cords, String sound, float volume, float pitch, int times) {
        playSound(cords, sound, volume, pitch, 1, 0);
    }

    public static void playSound(int[] cords, String sound, float volume, float pitch, int times, int delay) {
        Minecraft.getMinecraft().theWorld.playSound(cords[0], cords[1], cords[2], sound, volume, pitch, false);
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        for (int i = 1; i < times; i++) {
            exec.schedule(new Runnable() {
                public void run() {
                    Minecraft.getMinecraft().theWorld.playSound(cords[0], cords[1], cords[2], sound, volume, pitch, false);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
}
