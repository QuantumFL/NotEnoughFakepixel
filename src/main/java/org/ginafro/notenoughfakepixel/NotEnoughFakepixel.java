package org.ginafro.notenoughfakepixel;

import cc.polyfrost.oneconfig.events.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.ginafro.notenoughfakepixel.commands.NefCommand;
import org.ginafro.notenoughfakepixel.core.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.ginafro.notenoughfakepixel.features.duels.KDCounter;
import org.ginafro.notenoughfakepixel.features.skyblock.chocolate.ChocolateFactory;
import org.ginafro.notenoughfakepixel.features.skyblock.crimson.AshfangHelper;
import org.ginafro.notenoughfakepixel.features.skyblock.crimson.BossNotifier;
import org.ginafro.notenoughfakepixel.features.skyblock.crimson.AshfangOverlay;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.*;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.devices.*;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.mobs.BatMobDisplay;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.mobs.FelMobDisplay;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.mobs.StarredMobDisplay;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.puzzles.ThreeWeirdos;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.puzzles.WaterSolver;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.score.DungeonClearedNotifier;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.score.ScoreManager;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.score.ScoreOverlay;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.score.SPlusNotifier;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.terminals.*;
import org.ginafro.notenoughfakepixel.features.skyblock.enchanting.EnchantingSolvers;
import org.ginafro.notenoughfakepixel.features.skyblock.enchanting.HideEnchantingTooltips;
import org.ginafro.notenoughfakepixel.features.skyblock.enchanting.PreventMissclicks;
import org.ginafro.notenoughfakepixel.features.skyblock.fishing.GreatCatchNotifier;
import org.ginafro.notenoughfakepixel.features.skyblock.mining.*;
import org.ginafro.notenoughfakepixel.features.skyblock.overlays.StorageOverlay;
import org.ginafro.notenoughfakepixel.features.skyblock.qol.*;
import org.ginafro.notenoughfakepixel.features.skyblock.diana.*;
import org.ginafro.notenoughfakepixel.features.skyblock.slayers.*;
import org.ginafro.notenoughfakepixel.events.Handlers.PacketHandler;
import org.ginafro.notenoughfakepixel.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Mod(modid = "notenoughfakepixel", useMetadata=true)
public class NotEnoughFakepixel {

    public static Configuration config;
    Minecraft mc = Minecraft.getMinecraft();
    public static Logger logger;
    public File file;
    public static JsonObject roomsJson;
    public static JsonObject waypointsJson;
    public static List<String> motd = null;
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config = new Configuration();
        //ClientCommandHandler.instance.registerCommand(new TestCommand());
        //ClientCommandHandler.instance.registerCommand(new SlayerInfoCommand());
        ClientCommandHandler.instance.registerCommand(new NefCommand());

        MinecraftForge.EVENT_BUS.register(this);
        registerModEvents();


         try {
            ResourceLocation roomsLoc = new ResourceLocation( "dungeonrooms","dungeonrooms.json");
            InputStream roomsIn = Minecraft.getMinecraft().getResourceManager().getResource(roomsLoc).getInputStream();
            BufferedReader roomsReader = new BufferedReader(new InputStreamReader(roomsIn));

            ResourceLocation waypointsLoc = new ResourceLocation( "dungeonrooms","secretlocations.json");
            InputStream waypointsIn = Minecraft.getMinecraft().getResourceManager().getResource(waypointsLoc).getInputStream();
            BufferedReader waypointsReader = new BufferedReader(new InputStreamReader(waypointsIn));

            Gson gson = new Gson();
            roomsJson = gson.fromJson(roomsReader, JsonObject.class);

            waypointsJson = gson.fromJson(waypointsReader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerModEvents() {
        // Dungeons
        DungeonsMap map = new DungeonsMap();
        MinecraftForge.EVENT_BUS.register(map);
        EventManager.INSTANCE.register(map);

        //MinecraftForge.EVENT_BUS.register(new Testing());

        MinecraftForge.EVENT_BUS.register(new WelcomeMessage());
        MinecraftForge.EVENT_BUS.register(new SalvageItemsSaver());

        MinecraftForge.EVENT_BUS.register(new StartingWithSolver());
        MinecraftForge.EVENT_BUS.register(new ClickOnColorsSolver());
        MinecraftForge.EVENT_BUS.register(new ClickInOrderSolver());
        MinecraftForge.EVENT_BUS.register(new MazeSolver());
        MinecraftForge.EVENT_BUS.register(new CorrectPanesSolver());
        MinecraftForge.EVENT_BUS.register(new FirstDeviceSolver());
        MinecraftForge.EVENT_BUS.register(new ThirdDeviceSolver());
        MinecraftForge.EVENT_BUS.register(new HideTooltips());

        MinecraftForge.EVENT_BUS.register(new AutoReadyDungeon());
        MinecraftForge.EVENT_BUS.register(new AutoCloseChests());

        MinecraftForge.EVENT_BUS.register(new ThreeWeirdos());
        MinecraftForge.EVENT_BUS.register(new WaterSolver());

        MinecraftForge.EVENT_BUS.register(new WitherBloodKeysTracers());
        MinecraftForge.EVENT_BUS.register(new StarredMobDisplay());
        MinecraftForge.EVENT_BUS.register(new BatMobDisplay());
        MinecraftForge.EVENT_BUS.register(new FelMobDisplay());
        MinecraftForge.EVENT_BUS.register(new ItemSecretsDisplay());

        MinecraftForge.EVENT_BUS.register(new DungeonManager());
        MinecraftForge.EVENT_BUS.register(new ScoreManager());
        MinecraftForge.EVENT_BUS.register(new ScoreOverlay());
        MinecraftForge.EVENT_BUS.register(new SPlusNotifier());
        MinecraftForge.EVENT_BUS.register(new DungeonClearedNotifier());
        MinecraftForge.EVENT_BUS.register(new AutoRoom());
        MinecraftForge.EVENT_BUS.register(new Waypoints());
        MinecraftForge.EVENT_BUS.register(new MuteIrrelevantMessages());

        // Mining
        MinecraftForge.EVENT_BUS.register(new MiningOverlay());
        MinecraftForge.EVENT_BUS.register(new DrillFuelParsing());
        MinecraftForge.EVENT_BUS.register(new AbilityNotifier());
        MinecraftForge.EVENT_BUS.register(new EventsMsgSupressor());
        MinecraftForge.EVENT_BUS.register(new DrillFix());
        MinecraftForge.EVENT_BUS.register(new PuzzlerSolver());
        MinecraftForge.EVENT_BUS.register(new RemoveGhostInvis());
        // Fishing
        MinecraftForge.EVENT_BUS.register(new GreatCatchNotifier());
        // Enchanting
        MinecraftForge.EVENT_BUS.register(new EnchantingSolvers());
        //MinecraftForge.EVENT_BUS.register(new SuperpairsSolver());
        MinecraftForge.EVENT_BUS.register(new HideEnchantingTooltips());
        MinecraftForge.EVENT_BUS.register(new PreventMissclicks());

        // Chocolate Factory
        MinecraftForge.EVENT_BUS.register(new ChocolateFactory());
        // QOL
        MinecraftForge.EVENT_BUS.register(new ShowCurrentPet());
        MinecraftForge.EVENT_BUS.register(new ChatCleaner());
        MinecraftForge.EVENT_BUS.register(new VisualCooldowns());
        MinecraftForge.EVENT_BUS.register(new MiddleClickEvent());
        MinecraftForge.EVENT_BUS.register(new SoundRemover());
        MinecraftForge.EVENT_BUS.register(new ScrollableTooltips());
        //MinecraftForge.EVENT_BUS.register(new SlotLocking());
        MinecraftForge.EVENT_BUS.register(new StorageOverlay.StorageEvent());
        MinecraftForge.EVENT_BUS.register(new AutoOpenMaddox());
        MinecraftForge.EVENT_BUS.register(new MidasStaff());
        MinecraftForge.EVENT_BUS.register(new WardrobeShortcut());
        MinecraftForge.EVENT_BUS.register(new PetsShortcut());
        MinecraftForge.EVENT_BUS.register(new WarpsShortcut());

        MinecraftForge.EVENT_BUS.register(new Fullbright());
        MinecraftForge.EVENT_BUS.register(new KDCounter());
        // Diana
        MinecraftForge.EVENT_BUS.register(new Diana());
        // Crimson
        MinecraftForge.EVENT_BUS.register(new AshfangOverlay());
        MinecraftForge.EVENT_BUS.register(new BossNotifier());
        MinecraftForge.EVENT_BUS.register(new AshfangHelper());
        // Slayer
        MinecraftForge.EVENT_BUS.register(new SlayerMobsDisplay());
        MinecraftForge.EVENT_BUS.register(new VoidgloomSeraph());
        MinecraftForge.EVENT_BUS.register(new FirePillarDisplay());
        MinecraftForge.EVENT_BUS.register(new MinibossAlert());

      
        // Parsers
        MinecraftForge.EVENT_BUS.register(new TablistParser());
        MinecraftForge.EVENT_BUS.register(new ScoreboardUtils());

    }

    public static GuiScreen openGui;
    public static long lastOpenedGui;
    public int theme = 0;
    public static String th = "default";
    public static ResourceLocation bg = new ResourceLocation("notenoughfakepixel:backgrounds/" + th + "/background.png");

    public String getTheme(){
        return th;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e){
        if (e.phase != TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().thePlayer == null) {
            openGui = null;
            return;
        }
        if(Configuration.theme != theme){
            this.theme = Configuration.theme;
            if(Configuration.theme == 0){
                th = "default";
            }else if(Configuration.theme == 1){
                th = "dark";
            }else if(Configuration.theme == 2){
                th = "ocean";
            }
            bg = new ResourceLocation("notenoughfakepixel:backgrounds/" + th + "/background.png");
        }
        if (openGui != null) {
            if (Minecraft.getMinecraft().thePlayer.openContainer != null) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            }
            Minecraft.getMinecraft().displayGuiScreen(openGui);
            openGui = null;
            lastOpenedGui = System.currentTimeMillis();
        }

        ScoreboardUtils.parseScoreboard();

    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        event.manager.channel().pipeline().addBefore("packet_handler", "nef_packet_handler", new PacketHandler());
        System.out.println("Added packet handler to channel pipeline.");
    }

    @SubscribeEvent
    public void renderPlayerInfo(final RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (ScoreboardUtils.inDungeons) {
            if (AutoRoom.guiToggled) {
                AutoRoom.renderText();
            }
            if (AutoRoom.coordToggled) {
                AutoRoom.renderCoord();
            }
        }
    }
}
