package org.ginafro.notenoughfakepixel.features.skyblock.overlays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class StorageOverlay extends GuiScreen {
    public final static int maxButtons = 18;
    public final static int minButtons = 1;
    public static int buttonCount = 0;
    public static int enderChests = 0;
    public final GuiScreen g;
    public final GuiContainer gc;
    public int boxWidth,boxHeight,xPos,yPos;
    public int buttonWidth,buttonHeight;
    public int buttonListHeight;
    public float scale;
    private int scrollOffset = 0; // Tracks scrolling
    private final int ROWS_VISIBLE = 3;
    private final int BUTTONS_PER_ROW = 3;

    public StorageOverlay(GuiScreen gs){
        g = gs;
        gc = (GuiContainer) gs;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        buttonCount = 0;
        enderChests = 0;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        scale = (float) Minecraft.getMinecraft().displayWidth/width;
        boxWidth = (sr.getScaledWidth() / 8) * 6;
        boxHeight = (sr.getScaledHeight() / 8) * 5;
        xPos = sr.getScaledWidth() / 8;
        yPos = sr.getScaledHeight() / 16;
        buttonListHeight = boxHeight - sr.getScaledHeight() / 16;
        buttonWidth = 110;
        buttonHeight = 60;
    for(int i = 0;i < 9;i++){
        if(gc.inventorySlots.getSlot(9 + i).getStack().getDisplayName().toLowerCase().contains("ender")){
            buttonCount++;
            enderChests++;
        }
    }
    if(enderChests > 9){
        //System.out.println(enderChests);
        enderChests = 9;
    }
    for(int i = 0;i < 18;i++){
        if(!gc.inventorySlots.getSlot(27 + i).getStack().getDisplayName().toLowerCase().contains("empty")){
            buttonCount++;
        }
    }
    int xIndex = 0;
    int yIndex = 0;
    int minus = 0;
    for(int i = 0; i < buttonCount;i++){
        ResourceLocation rl = new ResourceLocation("notenoughfakepixel","skyblock/storage/enderchest/enderchest_size_1.png");
        if(i+1 > enderChests){
            minus = enderChests;
            rl = new ResourceLocation("notenoughfakepixel","skyblock/storage/backpacks/backpack_size_5.png");
        }

        this.buttonList.add(new StorageButton(i, 0, 0, buttonWidth, buttonHeight, rl,(i+1)-minus));
        xIndex++;
        if(xIndex > 2){
            yIndex += 1;
            xIndex = 0;
        }
    }
        updateVisibleButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ResourceLocation bg = new ResourceLocation("notenoughfakepixel","backgrounds/dark/background.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(bg);
        GuiScreen.drawModalRectWithCustomSizedTexture(xPos,yPos,0f,0f,boxWidth,buttonListHeight,boxWidth,buttonListHeight);
        ResourceLocation inv = new ResourceLocation("notenoughfakepixel","skyblock/storage/inventory.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(inv);
        GuiScreen.drawModalRectWithCustomSizedTexture(this.width/2 - 87,yPos + buttonListHeight + 5,0f,0f,174,105,174,105);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        GL11.glScissor(xPos * scaleFactor, (mc.displayHeight - (yPos + buttonListHeight) * scaleFactor), boxWidth * scaleFactor, buttonListHeight * scaleFactor);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        int xIndex = 0;
        int yIndex = 0;
        for(int i = 0; i < Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.size();i++){
            Slot slot = Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i);
            ItemStack stack = slot.getStack();
            if(stack != null && stack.stackSize > 0) {
                int y = yPos + buttonListHeight + 8 + (yIndex * 20);
                int x = this.width / 2 - 83 + (xIndex * 20);
                GlStateManager.color(1.0f,1.0f,1.0f);
                    Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y );
                    if(stack.stackSize > 1) {
                        fontRendererObj.drawString(String.valueOf(stack.stackSize), x + 14, y + 14, -1);
                    }
                    if(mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16){
                        this.renderToolTip(stack,mouseX,mouseY);
                    }
            }
            xIndex++;
            if (xIndex > 8) {
                xIndex = 0;
                yIndex++;
            }
        }

    }

    private void updateVisibleButtons() {
        int startIndex = scrollOffset * BUTTONS_PER_ROW;
        int endIndex = Math.min(startIndex + ROWS_VISIBLE * BUTTONS_PER_ROW, buttonList.size());

        int xIndex = 0;
        int yIndex = 0;
        for (int i = 0; i < buttonList.size(); i++) {
            GuiButton button = buttonList.get(i);
            if (i >= startIndex && i < endIndex) {
                button.visible = true;
                button.xPosition = xPos + 35 + xIndex * (buttonWidth + 5);
                button.yPosition = yPos + 5 + yIndex * (buttonHeight + 5);
                xIndex++;
                if (xIndex >= BUTTONS_PER_ROW) {
                    xIndex = 0;
                    yIndex++;
                }
            } else {
                button.visible = false;
            }
        }
        for(GuiButton button : buttonList){
            if(button instanceof StorageButton){
                if(button.yPosition + button.height > yPos + buttonListHeight){
                    button.visible = false;
                }else{
                    button.visible = true;
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button instanceof StorageButton){
            StorageButton b = (StorageButton) button;
            b.process(this.gc);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int maxScrollOffset = Math.max(0, (buttonCount + BUTTONS_PER_ROW) / BUTTONS_PER_ROW - ROWS_VISIBLE);
            scrollOffset = Math.max(0, Math.min(scrollOffset - wheel / 120, maxScrollOffset));
            updateVisibleButtons();
        }
    }


    public static class StorageEvent {

    @SubscribeEvent
    public void onOpen(GuiScreenEvent.BackgroundDrawnEvent e) {
        if (e.gui instanceof GuiContainer) {
            GuiContainer gc = (GuiContainer) e.gui;
            if(gc instanceof GuiInventory) return;
            if (gc.inventorySlots.getSlot(4).getStack() != null) {
                if (gc.inventorySlots.getSlot(4).getStack().getDisplayName().contains("Ender")) {
                    if (Configuration.storageOverlay) {
                        Minecraft.getMinecraft().displayGuiScreen(new StorageOverlay(e.gui));
                    }
                }
            }
            }
        }
    }
}
