package org.ginafro.notenoughfakepixel.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.variables.DungeonFloor;
import org.ginafro.notenoughfakepixel.variables.Gamemode;
import org.ginafro.notenoughfakepixel.variables.Area;
import org.ginafro.notenoughfakepixel.variables.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScoreboardUtils {

    public static Area currentArea = Area.NONE;
    public static Gamemode currentGamemode = Gamemode.LOBBY;
    public static Location currentLocation = Location.NONE;

    public static boolean inDungeons = false;
    public static DungeonFloor currentFloor = DungeonFloor.NONE;
    public static int clearedPercentage = -1;

    private static Pattern floorPattern = Pattern.compile(" §7⏣ §cThe Catacombs §7\\(<?floor>.{2}\\)");

    public static void parseScoreboard() {
        Minecraft mc = Minecraft.getMinecraft();

        if (!mc.isSingleplayer() && mc.getCurrentServerData().serverIP.contains("fakepixel")) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();

            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective != null) {
                String objName = ScoreboardUtils.cleanSB(objective.getDisplayName());
                currentGamemode = Gamemode.getGamemode(objName);

                // getting player names for that objective
                Collection<Score> scoreCollection = scoreboard.getSortedScores(objective);
                List<Score> scoreList = scoreCollection.stream()
                        .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
                        .collect(Collectors.toList());

                for (Score score : scoreList) {
                    String playerName = score.getPlayerName();
                    if (playerName.startsWith(" §7⏣ §cThe Catacombs §7")) {
                        String floor = score.getPlayerName()
                                .replaceAll(" §7⏣ §cThe Catacombs §7\\(", "")
                                .replaceAll("\\)", "");

                        currentFloor = DungeonFloor.getFloor(floor);
                    }


                    if (playerName.startsWith("§fDungeon Cleared: ")){
                        String cleanString = StringUtils.stripControlCodes(playerName);

                        String percentage = cleanString
                                .replaceAll("Dungeon Cleared: ", "")
                                .replaceAll("%", "");

                        clearedPercentage = Integer.parseInt(percentage);
                    }
                }
            }
        }

        if (currentGamemode == Gamemode.SKYBLOCK && !mc.isSingleplayer() && mc.getNetHandler() != null) {
            for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                IChatComponent s1 = playerInfo.getDisplayName();
                if (s1 != null) {
                    String name = StringUtils.stripControlCodes(s1.getUnformattedText());
                    if (name.contains("Server: ")) {
                        currentLocation = Location.getLocation(
                                name.replace("Server: ", "")
                                        .replaceFirst("-\\d+", "-")
                                        .replaceAll("\\s+","")
                        );
                    }
                    if (name.contains("Area")) {
                        currentGamemode = Gamemode.SKYBLOCK;
                        currentArea = Area.getArea(name.replace("Area: ", ""));
                    } else if (name.contains("Dungeon")) {
                        currentGamemode = Gamemode.SKYBLOCK;
                        currentLocation = Location.DUNGEON;
                        inDungeons = true;
                    }
                }
            }
        }
    }

    @SuppressWarnings({"ExecutionException", "IllegalArgumentException"})
    public static List<String> getScoreboard() {
        List<String> lines = new ArrayList<>();
        if (Minecraft.getMinecraft().theWorld == null) return lines;
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return lines;

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
                        .startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }

    public static boolean scoreboardContains(String string) {
        boolean result = false;
        List<String> scoreboard = getScoreboard();
        for (String line : scoreboard) {
            line = cleanSB(line);
            line = Utils.removeFormatting(line);
            if(line.contains(string)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static int getHubNumber() {
        if (Minecraft.getMinecraft().getNetHandler() != null) {
            //System.out.println("Point 1 reached");
            for (NetworkPlayerInfo playerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
                IChatComponent s1 = playerInfo.getDisplayName();
                if (s1 != null) {
                    String name = StringUtils.stripControlCodes(s1.getUnformattedText());
                    //System.out.println(name);
                    if (name.contains("Server: ")) {
                        String serverName = name.replace("Server: ", "").replaceAll("\\s+", "");
                        Pattern hubPattern = Pattern.compile("skyblock-(\\d+)");
                        Matcher matcher = hubPattern.matcher(serverName);

                        if (matcher.find()) {
                            String hubNumber = matcher.group(1); // Capturamos el número de HUB
                            return Integer.parseInt(hubNumber);
                        }
                    }
                }
            }
        } return -1;
    }



    public static String cleanSB(String scoreboard) {
        char[] nvString = StringUtils.stripControlCodes(scoreboard).toCharArray();
        StringBuilder cleaned = new StringBuilder();

        for (char c : nvString) {
            if ((int) c > 20 && (int) c < 127) {
                cleaned.append(c);
            }
        }

        return cleaned.toString();
    }

    public static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        if (Minecraft.getMinecraft().theWorld == null) return lines;
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return lines;

        Collection<Score> scores;
        try {
            scores = scoreboard.getSortedScores(objective);
        } catch (ConcurrentModificationException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }

        List<Score> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
                        .startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        currentLocation = Location.NONE;
        currentGamemode = Gamemode.LOBBY;
        currentArea = Area.NONE;
        currentFloor = DungeonFloor.NONE;
        clearedPercentage = -1;
    }

}