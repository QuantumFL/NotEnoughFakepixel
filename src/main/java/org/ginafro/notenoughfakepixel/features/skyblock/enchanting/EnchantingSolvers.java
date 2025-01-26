package org.ginafro.notenoughfakepixel.features.skyblock.enchanting;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.utils.RenderUtils;
import org.ginafro.notenoughfakepixel.utils.TablistParser;
import org.ginafro.notenoughfakepixel.variables.F7ColorsDict;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EnchantingSolvers {

    public static SolverTypes currentSolverType = SolverTypes.NONE;
    private static List<UltrasequencerSlot> ultrasequencerSlots = new ArrayList<>();
    private static List<Integer> chronomatronOrder = new ArrayList<Integer>();
    private int previousIndex = 0;
    private boolean noteFinished = true;
    private boolean resolved = false;
    private boolean resolving = false;
    private Color green = new Color(0, 255, 0);
    private int slotToClickUltrasequencer = 1;

    private class UltrasequencerSlot{
        public Slot slot;
        public int quantity;

        public UltrasequencerSlot(Slot slot, int quantity){
            this.slot = slot;
            this.quantity = quantity;
        }
    }

    private enum SolverTypes {
        NONE,
        CHRONOMATRON,
        ULTRASEQUENCER,
        SUPERPAIRS
    }

    private SolverState solverState = SolverState.NONE;

    private enum SolverState {
        NONE,
        REMEMBERING,
        SOLVING
    }

    @SubscribeEvent()
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null) return;
        if (!(event.gui instanceof GuiChest)) return;

        String chestName = TablistParser.currentOpenChestName;
        if (chestName == null || chestName.isEmpty()) return;

        if (chestName.startsWith("Chronomatron")) {
            currentSolverType = SolverTypes.CHRONOMATRON;
        } else if (chestName.startsWith("Ultrasequencer")) {
            currentSolverType = SolverTypes.ULTRASEQUENCER;
        } else if (chestName.startsWith("Super Pairs")) {
            currentSolverType = SolverTypes.SUPERPAIRS;
        } else if (chestName.startsWith("Experiment Over")) {
            currentSolverType = SolverTypes.NONE;
        } else {
            currentSolverType = SolverTypes.NONE;
        }
        resolving = false;
    }

    @SubscribeEvent
    public void onGuiDrawn(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (Configuration.ultraSequencerSolver && currentSolverType == SolverTypes.ULTRASEQUENCER) {

            if(!(event.gui instanceof GuiChest)) return;
            GuiChest chest = (GuiChest) event.gui;
            Container container = chest.inventorySlots;

            if(!(container instanceof ContainerChest)) return;
            String title = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
            if (!title.startsWith("Ultrasequencer (")) return;
            ContainerChest containerChest = (ContainerChest) container;
            // Check if its in remember state
            IInventory lower = ((ContainerChest) container).getLowerChestInventory();
            ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
            if (timerStack == null) return;
            boolean isClock = timerStack.getItem() == Items.clock;

            // if not clock, then remember the items
            if (!isClock){
                resolving = false;
                ultrasequencerSlots = new ArrayList<>();
                for(Slot slot : containerChest.inventorySlots) {
                    // select only the items in the chest
                    if (slot.inventory == Minecraft.getMinecraft().thePlayer.inventory) continue;
                    ItemStack item = slot.getStack();
                    if (item == null) continue;


                    if (item.getItem() == Items.dye) {

                        int stackSize = item.stackSize;
                        ultrasequencerSlots.add(new UltrasequencerSlot(slot, stackSize));
                    }
                }
                slotToClickUltrasequencer = 1;
            } else {
                resolving = true;
                // if its clock, draw the items in the list
                for(UltrasequencerSlot slot : ultrasequencerSlots){
                    ItemStack itemInSlot = containerChest.inventorySlots.get(slot.slot.slotNumber).getStack();
                    if (itemInSlot == null) continue;
                    if (itemInSlot.getItem() == Items.dye) continue;

                    RenderUtils.drawOnSlot(containerChest.inventorySlots.size(), slot.slot.xDisplayPosition, slot.slot.yDisplayPosition, new Color(255, 0, 0).getRGB(), slot.quantity);
                }
            }
        } else if (Configuration.chronomatronSolver && currentSolverType == SolverTypes.CHRONOMATRON){
            if(!(event.gui instanceof GuiChest)) return;
            GuiChest chest = (GuiChest) event.gui;
            Container container = chest.inventorySlots;

            if(!(container instanceof ContainerChest)) return;
            String title = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
            if (!title.startsWith("Chronomatron (")) return;
            ContainerChest containerChest = (ContainerChest) container;
            // Check if its in remember state
            IInventory lower = ((ContainerChest) container).getLowerChestInventory();
            ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
            if (timerStack == null) return;
            boolean isClock = timerStack.getItem() == Items.clock;
            // if is not clock, then remember the items
            if (!isClock) {
                if (resolving) chronomatronOrder.clear();
                resolving = false;
                // getting item in slot 4
                ItemStack itemInSlot = containerChest.inventorySlots.get(4).getStack();
                // checking its stack quantity
                if (itemInSlot == null) return;
                int round = itemInSlot.stackSize;
                if (chronomatronOrder.size() >= round) return;

                Item lastItem = containerChest.inventorySlots.get(previousIndex).getStack().getItem();
                if (Block.getBlockFromItem(lastItem) == Blocks.stained_glass) {
                    noteFinished = true;
                }
                if (!noteFinished) return;

                List<Integer> slotRanges = new ArrayList<>();

                for (int i = 9; i <= 18; i++) {
                    slotRanges.add(i);
                }

                if (TablistParser.currentOpenChestName.contains("Transcendent") ||
                        TablistParser.currentOpenChestName.contains("Metaphysical")) {
                    for (int i = 28; i <= 37; i++) {
                        slotRanges.add(i);
                    }
                }

                for (int index : slotRanges) {
                    Slot slot = containerChest.inventorySlots.get(index);
                    ItemStack item = slot.getStack();

                    if (item == null) continue;

                    if (Block.getBlockFromItem(item.getItem()) != Blocks.stained_hardened_clay) {
                        continue;
                    }

                    previousIndex = slot.getSlotIndex();
                    chronomatronOrder.add(previousIndex);
                    noteFinished = false;
                    break;
                }

            } else {
                resolving = true;
                if (!chronomatronOrder.isEmpty()) {
                    int resultIndex = chronomatronOrder.get(0);
                    Item resultItem = containerChest.inventorySlots.get(resultIndex).getStack().getItem();
                    if (Block.getBlockFromItem(resultItem) == Blocks.stained_glass) {
                        if (resolved) {
                            chronomatronOrder.remove(0);
                            resolved = false;
                            return;
                        }
                        Slot slot1 = containerChest.inventorySlots.get(resultIndex);
                        Slot slot2 = containerChest.inventorySlots.get(resultIndex+9);
                        RenderUtils.drawOnSlot(containerChest.inventorySlots.size(), slot1.xDisplayPosition, slot1.yDisplayPosition, green.getRGB());
                        RenderUtils.drawOnSlot(containerChest.inventorySlots.size(), slot2.xDisplayPosition, slot2.yDisplayPosition, green.getRGB());
                        if (!TablistParser.currentOpenChestName.contains("Transcendent") && !TablistParser.currentOpenChestName.contains("Metaphysical")) {
                            Slot slot3 = containerChest.inventorySlots.get(resultIndex+18);
                            RenderUtils.drawOnSlot(containerChest.inventorySlots.size(), slot3.xDisplayPosition, slot3.yDisplayPosition, green.getRGB());
                        }
                    } else if (Block.getBlockFromItem(resultItem) == Blocks.stained_hardened_clay && !resolved) {
                        resolved = true;
                    }

                }
                previousIndex = 0;
                noteFinished = true;
            }
        }
        else if (currentSolverType == SolverTypes.NONE){
            if (chronomatronOrder.isEmpty() && ultrasequencerSlots.isEmpty()) return;

            //System.out.println("Deleting data");
            ultrasequencerSlots = new ArrayList<>();
            chronomatronOrder = new ArrayList<>();
            previousIndex = 0;
            noteFinished = true;
            resolved = false;
            resolving = false;
            slotToClickUltrasequencer = 1;
        }
    }

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Mouse.getEventButtonState()) return;
        if (!Configuration.preventMissclicksExperimentation) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return; // Check if the current screen is a chest GUI
        GuiChest chestGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
        if (chestGui.getSlotUnderMouse() == null) return;
        int slotIndex = chestGui.getSlotUnderMouse().getSlotIndex();
        if (Configuration.chronomatronSolver && currentSolverType == SolverTypes.CHRONOMATRON && resolving && !chronomatronOrder.isEmpty()) {
            if (slotIndex == chronomatronOrder.get(0) ||
                    slotIndex == chronomatronOrder.get(0) + 9 ||
                    (slotIndex == chronomatronOrder.get(0) + 18 && !TablistParser.currentOpenChestName.contains("Transcendent") && !TablistParser.currentOpenChestName.contains("Metaphysical"))) {
                return; // Valid case, no need to cancel the event
            }
            event.setCanceled(true);
        } else if (Configuration.ultraSequencerSolver && currentSolverType == SolverTypes.ULTRASEQUENCER && resolving) {
            for(UltrasequencerSlot slot : ultrasequencerSlots){
                //System.out.println(slotToClickUltrasequencer + ", " + slot.quantity);
                ItemStack itemInSlot = chestGui.inventorySlots.getInventory().get(slotIndex);
                if (slot.slot == chestGui.getSlotUnderMouse() && slotToClickUltrasequencer == slot.quantity) {
                    slotToClickUltrasequencer++;
                    return;
                }
                if (itemInSlot == null) continue;
                if (itemInSlot.getItem() == Items.dye) continue;
            }
            event.setCanceled(true); // cancel click if not found
        }
    }

    /*@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDisplayTooltip(ItemTooltipEvent event) {
        if (event.isCancelable()) event.setCanceled(true);
        event = new ItemTooltipEvent(event.itemStack, event.entityPlayer, null, false);
    }/*

    /*public class CustomTooltipHandler extends IContainerTooltipHandler {
        @override
        public void shouldShow
    }*/
}