package com.silverminer.color_block.util;

import java.util.function.Supplier;

import com.silverminer.color_block.init.InitItems;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroups extends ItemGroup {

	/**
	 * The texture of the given item is the icon of the Item group
	 */
	private final Supplier<ItemStack> iconSupplier;

	/**
	 * This is an mod item group: first param is the key of the Itemgroup. Set the
	 * Name it Language *.json file. The second is an supplier of the Item that
	 * gives the group the Icon
	 */
	public static final ItemGroup COLOR_BLOCK = new ModItemGroups("color_block",
			() -> new ItemStack(InitItems.COLOR_BLOCK.get()));

	/**
	 * This Constucture is used to create an mod Item group
	 * 
	 * @param name         This is the key of the item group. Set the name in
	 *                     Language file
	 * @param iconSupplier This is the Item that gives the item group the icon
	 */
	public ModItemGroups(final String name, final Supplier<ItemStack> iconSupplier) {
		super(name);
		this.iconSupplier = iconSupplier;
	}

	/**
	 * This Method returns the {@link ItemStack} that gives the item group the icon
	 */
	@Override
	public ItemStack createIcon() {
		return iconSupplier.get();
	}

}