package com.silverminer.color_block;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.gui.screen.ColorBlockScreen;
import com.silverminer.color_block.gui.screen.ImageScreen;
import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.init.InitContainerType;
import com.silverminer.color_block.init.InitItems;
import com.silverminer.color_block.util.events.ClientEvents;
import com.silverminer.color_block.util.saves.ColorBlockSaveHelper;
import com.silverminer.color_block.util.saves.ColorBlocksSavedData;

@Mod(ColorBlockMod.MODID)
@Mod.EventBusSubscriber(modid = ColorBlockMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColorBlockMod {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger(ColorBlockMod.MODID);
	// The value here should match an entry in the META-INF/mods.toml file
	public static final String MODID = "color_block";

	// The list of placed Color Blocks. It's an Helper that contains the BlockPos,
	// the String of Dimension Name and the Color of the Block at the place
	public static final ArrayList<ColorBlockSaveHelper> COLOR_BLOCKS = new ArrayList<ColorBlockSaveHelper>();

	// This is used to save and read the list on top
	public static ColorBlocksSavedData colorBlocksSavedData;

	// The Constuctur is called wether this is named in META-INF/mods.toml
	/**
	 * The Constuctur registers Listeners and {@link DeferredRegister} init
	 */
	public ColorBlockMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.register(ClientEvents.class);
		InitItems.ITEMS.register(modEventBus);
		InitBlocks.BLOCKS.register(modEventBus);
		InitContainerType.CONTAINER.register(modEventBus);
	}

	/**
	 * This Event is used to register Rendertypes of blocks or Screens
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public static void setupClient(FMLClientSetupEvent event) {
		ColorBlockMod.LOGGER.info("Registering Renderers and Screens");

		ScreenManager.registerFactory(InitContainerType.COLOR_BLOCK.get(), ColorBlockScreen::new);
		ScreenManager.registerFactory(InitContainerType.IMAGE_CONTAINER.get(), ImageScreen::new);
	}
}