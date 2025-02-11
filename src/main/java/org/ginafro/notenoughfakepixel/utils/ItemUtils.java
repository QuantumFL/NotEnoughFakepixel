package org.ginafro.notenoughfakepixel.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.jetbrains.annotations.NotNull;
import java.util.regex.Pattern;

public class ItemUtils {

    public static String getInternalName(ItemStack item){
        if(!item.hasTagCompound()) return "";
        if(!item.getTagCompound().hasKey("ExtraAttributes")) return "";

        NBTTagCompound extraAttributes = item.getTagCompound().getCompoundTag("ExtraAttributes");
        if(!extraAttributes.hasKey("id")) return "";

        return extraAttributes.getString("id");
    }

    public static @NotNull NBTTagCompound getExtraAttributes(ItemStack itemStack) {
        NBTTagCompound tag = getOrCreateTag(itemStack);
        NBTTagCompound extraAttributes = tag.getCompoundTag("ExtraAttributes");
        tag.setTag("ExtraAttributes", extraAttributes);
        return extraAttributes;
    }

    public static int getExtraAttributesIntTag(ItemStack item, String tag){
        NBTTagCompound extraAttributes = getExtraAttributes(item);
        if(!extraAttributes.hasKey(tag)) return -1;
        return extraAttributes.getInteger(tag);
    }

    public static String getExtraAttributesStringTag(ItemStack item, String tag){
        NBTTagCompound extraAttributes = getExtraAttributes(item);
        if(!extraAttributes.hasKey(tag)) return "";
        return extraAttributes.getString(tag);
    }

    public static NBTTagCompound getOrCreateTag(ItemStack is) {
        if (is.hasTagCompound()) return is.getTagCompound();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        is.setTagCompound(nbtTagCompound);
        return nbtTagCompound;
    }

    public static void addLoreLine(ItemStack item, String line){
        if(!item.hasTagCompound()) return;
        if(!item.getTagCompound().hasKey("display")) return;
        if(!item.getTagCompound().getCompoundTag("display").hasKey("Lore")) return;

        NBTTagCompound displayTag = item.getTagCompound().getCompoundTag("display");
        NBTTagList lore = displayTag.getTagList("Lore", 8);

        lore.appendTag(new NBTTagString(line));
    }

    public static String getLoreLine(ItemStack item, String matcher){
        if(!item.hasTagCompound()) return null;
        if(!item.getTagCompound().hasKey("display")) return null;
        if(!item.getTagCompound().getCompoundTag("display").hasKey("Lore")) return null;

        NBTTagCompound displayTag = item.getTagCompound().getCompoundTag("display");
        NBTTagList lore = displayTag.getTagList("Lore", 8);

        for(int i = 0;i < lore.tagCount(); i++){
            String line = lore.getStringTagAt(i);
            if(line.contains(matcher)) return line;
        }

        return null;
    }

    public static String getLoreLine(ItemStack item, Pattern matcher){
        if(!item.hasTagCompound()) return null;
        if(!item.getTagCompound().hasKey("display")) return null;
        if(!item.getTagCompound().getCompoundTag("display").hasKey("Lore")) return null;

        NBTTagCompound displayTag = item.getTagCompound().getCompoundTag("display");
        NBTTagList lore = displayTag.getTagList("Lore", 8);

        for(int i = 0;i < lore.tagCount(); i++){
            String line = lore.getStringTagAt(i);
            if (matcher.matcher(line).matches()) return line;

        }

        return null;
    }

    public static boolean hasSkinValue(String value, ItemStack item){
        if (item == null) return false;
        if (!item.hasTagCompound()) return false;
        if (!item.getTagCompound().hasKey("SkullOwner")) return false;
        NBTTagCompound skullOwner = item.getTagCompound().getCompoundTag("SkullOwner");
        if (!skullOwner.hasKey("Properties")) return false;
        NBTTagCompound properties = skullOwner.getCompoundTag("Properties");
        if (!properties.hasKey("textures")) return false;
        NBTTagList textures = properties.getTagList("textures", 10);
        for (int i = 0; i < textures.tagCount(); i++) {
            NBTTagCompound texture = textures.getCompoundTagAt(i);
            if (texture.hasKey("Value") && texture.getString("Value").equals(value)) return true;
        }
        return false;
    }
}
