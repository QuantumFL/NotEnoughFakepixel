package org.ginafro.notenoughfakepixel.features.skyblock.dungeons.terminals;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.DungeonManager;
import org.ginafro.notenoughfakepixel.utils.RenderUtils;

import java.util.List;

public class MazeSolver {
    int[] adjacentPositions = new int[] {1,-1,9,-9};

    @SubscribeEvent
    public void onRender(GuiScreenEvent.BackgroundDrawnEvent e){
        if(!Configuration.dungeonsTerminalMazeSolver) return;
        if (!DungeonManager.checkEssentialsF7()) return;
        if(!(e.gui instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) e.gui;
        Container container = chest.inventorySlots;
        if(!(container instanceof ContainerChest)) return;
        ContainerChest containerChest = (ContainerChest) container;
        String name = containerChest.getLowerChestInventory().getDisplayName().getUnformattedText();
        if(name.contains("Complete the maze!")){
            for(Slot slot : containerChest.inventorySlots){
                if (slot.inventory == Minecraft.getMinecraft().thePlayer.inventory) continue;
                ItemStack item = slot.getStack();
                if(item == null) continue;
                if (Block.getBlockFromItem(item.getItem()) instanceof BlockStainedGlassPane) {
                    if (item.getMetadata() == 5) {
                        Slot targetSlot = getClosestWhiteSlot(containerChest.inventorySlots, slot);
                        if (targetSlot == null) continue;
                        RenderUtils.drawOnSlot(container.inventorySlots.size(), targetSlot.xDisplayPosition, targetSlot.yDisplayPosition, Configuration.dungeonsCorrectColor.getRGB());
                    }
                }
            }
        }

    }

    private Slot getClosestWhiteSlot(List<Slot> inventory, Slot slot) {
        for (int i:adjacentPositions) {
            if (slot.getSlotIndex()+i < 0 || slot.getSlotIndex()+i > 53) continue;
            try {
                if (inventory.get(slot.getSlotIndex() + i).getStack().getMetadata() == 0)
                    return inventory.get(slot.getSlotIndex() + i);
            } catch ( NullPointerException exception) {
                return null;
            }
        }
        return null;
    }
}
