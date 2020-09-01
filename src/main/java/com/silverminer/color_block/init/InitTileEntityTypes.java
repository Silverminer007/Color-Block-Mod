package com.silverminer.color_block.init;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.objects.tile_entity.*;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitTileEntityTypes {
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister
			.create(ForgeRegistries.TILE_ENTITIES, ColorBlockMod.MODID);

	public static final RegistryObject<TileEntityType<ColorBlockTileEntity>> COLOR_BLOCK = TILE_ENTITIES.register("color_block",
			() -> TileEntityType.Builder.create(ColorBlockTileEntity::new,
					InitBlocks.COLOR_BLOCK.get()).build(null));

	public static final RegistryObject<TileEntityType<ImageTileEntity>> IMAGE_BLOCK = TILE_ENTITIES.register("image_block",
			() -> TileEntityType.Builder.create(ImageTileEntity::new,
					InitBlocks.IMAGE_BLOCK.get()).build(null));
}