package org.ginafro.notenoughfakepixel.features.skyblock.qol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.utils.ChatUtils;
import org.ginafro.notenoughfakepixel.utils.SoundUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class WelcomeMessage {

    private boolean notified = false;
    private Random random = new Random();
    private String initialString = "[NEF] ";

    List<String> arrayWelcomeMessages = new ArrayList<>(Arrays.asList(
            "Did you know that, 9 out of 10 doctors recommend NEF?",
            "Did you know that, 9 out of 10 bugs reported are related to pojav?",
            "Fun Fact: 9 out of 10 Fakepixel players recommend NEF. The 10th player fell into the void",
            "Alert! Fakepixel detected a skill issue. Please report in NEF's Discord immediately",
            "Watch Out: Every time you miss a dungeon puzzle with NEF, a villager sighs in disappointment",
            "NEF gives you dungeon powers, better don't die into Goldor",
            "Attention: Your free trial of skill has expired. Please renew with NEF",
            "Did you know that, statistically, you’re worse at Minecraft than you think?",
            "NEF detected. Injecting 100% more gaming skill… please wait… process failed",
            "NEF makes you better at Minecraft. Too bad it can’t fix your hairline",
            "It is said that in the past, people could play Fakepixel without NEF",
            "Did you know that, NEF is younger than you? But maybe... more useful",
            "Welcome to Fakepixel! Did you know NEF is the secret ingredient to your gaming happiness?",
            "Welcome back to Fakepixel, where NEF makes everything better!",
            "Did you know NEF was the original inspiration for the world's best gaming experiences?",
            "Once upon a time, players tried Fakepixel without NEF... and the world was less fun",
            "The future of Fakepixel is NEF. Don’t worry, you’re on the right side of history",
            "We know you love NEF... and NEF also loves you!",
            "It is said that NEF was created for the legends of Fakepixel… and you're one of them!",
            "Once upon a time, one brave fighter played FakePixel without NEF. Wait, that's you",
            "Did you know that Fakepixel does have other gamemodes except Skyblock?",
            "@Iru98X wishes you a happy nef-gaming!",
            "Fun Fact: It took @_Whispering nearly 7 months to go from Helper to Moderator on Fakepixel",
            "Did you know that Samsung everyday day keeps Apple away? (@ClassyCoder)",
            "It always seems impossible until it’s done (@RealMG)",
            "Did you know that @Danielo is also #nef-addict?",
            "Did you know that NEF won't launch if you haven't @Danielo's enough?"
    ));

    String epicString = "You discovered an epic message that gives you 200% extra penetration for the rest of your day!";
    String legendaryString = "You discovered a legendary message that will make you not bald for the rest of your life!";


    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!notified && event.message.getUnformattedText().equals("+10 Magic Find Bonus!")) {
            notified = true;
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            String welcomeMessage = "Welcome "+player.getName()+"! ";
            String randomMessage = arrayWelcomeMessages.get(random.nextInt(arrayWelcomeMessages.size()));
            int numberSpecial = random.nextInt(1001);
            if (numberSpecial == 1000) {
                randomMessage = EnumChatFormatting.GOLD + "(LEGENDARY) " + EnumChatFormatting.BLUE + legendaryString;
            } else if (numberSpecial > 900) {
                randomMessage = EnumChatFormatting.DARK_PURPLE + "(EPIC) " + EnumChatFormatting.BLUE + epicString;
            }
            if (randomMessage.startsWith("Welcome")) welcomeMessage = "";
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            String finalRandomMessage = randomMessage;
            String finalWelcomeMessage = welcomeMessage;
            exec.schedule(() -> {
                ChatUtils.notifyChat(EnumChatFormatting.BOLD.toString() + EnumChatFormatting.BLUE + initialString + finalWelcomeMessage + finalRandomMessage);
                SoundUtils.playSound(new int[]{player.getPosition().getX(),
                        player.getPosition().getY(),
                        player.getPosition().getZ()},"note.pling",2.0f,1.0f);
            }, 1000, TimeUnit.MILLISECONDS);
            exec.schedule(() -> SoundUtils.playSound(new int[]{player.getPosition().getX(),
                    player.getPosition().getY(),
                    player.getPosition().getZ()},"note.pling",2.0f,1.6f), 1300, TimeUnit.MILLISECONDS);
        }
    }
}
