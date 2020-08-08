package com.silverminer.color_block.init;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.gui.container.ColorBlockContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitContainerType {
	/**
	 * This takes witch type want to be registered
	 */
	public static final DeferredRegister<ContainerType<?>> CONTAINER = DeferredRegister
			.create(ForgeRegistries.CONTAINERS, ColorBlockMod.MODID);

	/**
	 * Registers the color Block container to be in the game
	 */
	public static final RegistryObject<ContainerType<ColorBlockContainer>> COLOR_BLOCK = CONTAINER
			.register("color_block", () -> IForgeContainerType.create(ColorBlockContainer::new));
}