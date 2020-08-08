package com.silverminer.color_block.init;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitBlocks {
	/**
	 * Ermöglicht die Registierung von Blöcken
	 */
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			ColorBlockMod.MODID);

	/**
	 * Registiert den Farbblock
	 */
	public static final RegistryObject<Block> COLOR_BLOCK = BLOCKS.register("color_block",
			() -> new ColorBlock(Block.Properties.from(Blocks.STONE)));
}