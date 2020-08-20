package com.silverminer.color_block.init;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.objects.items.ColorToolItem;
import com.silverminer.color_block.util.ModItemGroups;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitItems {
	/**
	 * Ermöglicht die registierung von Items
	 */
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			ColorBlockMod.MODID);

	/**
	 * registiertt das Item um den Farbblock zu setzen
	 */
	public static final RegistryObject<Item> COLOR_BLOCK = ITEMS.register("color_block",
			() -> new BlockItem(InitBlocks.COLOR_BLOCK.get(), new Item.Properties().group(ModItemGroups.COLOR_BLOCK)));

	/**
	 * Registiert das Item um den Farbblock zu modifiezieren
	 */
	public static final RegistryObject<Item> COLOR_TOOL = ITEMS.register("color_tool",
			() -> new ColorToolItem(new Item.Properties().group(ModItemGroups.COLOR_BLOCK)));

	/**
	 * Registriert das Item um den Farbblock auf eine Zufällige Farbe zu setzen
	 */
	public static final RegistryObject<Item> MULTI_DYE = ITEMS.register("multi_dye",
			() -> new Item(new Item.Properties().group(ModItemGroups.COLOR_BLOCK)));

	public static final RegistryObject<Item> IMAGE_BLOCK = ITEMS.register("image_block",
			() -> new BlockItem(InitBlocks.IMAGE_BLOCK.get(), new Item.Properties().group(ModItemGroups.COLOR_BLOCK)));
}