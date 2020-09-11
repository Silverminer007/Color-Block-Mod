package com.silverminer.color_block;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.init.InitContainerType;
import com.silverminer.color_block.init.InitItems;
import com.silverminer.color_block.init.InitTileEntityTypes;
import com.silverminer.color_block.util.events.ClientEvents;
import com.silverminer.color_block.util.saves.ColorBlocksSavedData;

@Mod(ColorBlockMod.MODID)
public class ColorBlockMod {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger(ColorBlockMod.MODID);
	// The value here should match an entry in the META-INF/mods.toml file
	public static final String MODID = "color_block";

	// This is used to save and read the list on top
	public static ColorBlocksSavedData colorBlocksSavedData;

	public static File image_path;

	// The Constuctur is called wether this is named in META-INF/mods.toml
	/**
	 * The Constuctur registers Listeners and {@link DeferredRegister} init
	 */
	@SuppressWarnings("resource")
	public ColorBlockMod() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			image_path = new File(Minecraft.getInstance().gameDir.getAbsoluteFile(), "Images");
		}
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.register(ClientEvents.class);
		InitItems.ITEMS.register(modEventBus);
		InitBlocks.BLOCKS.register(modEventBus);
		InitContainerType.CONTAINER.register(modEventBus);
		InitTileEntityTypes.TILE_ENTITIES.register(modEventBus);
	}
}