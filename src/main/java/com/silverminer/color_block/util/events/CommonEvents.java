package com.silverminer.color_block.util.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.gui.screen.ColorBlockScreen;
import com.silverminer.color_block.gui.screen.ImageScreen;
import com.silverminer.color_block.init.InitContainerType;
import com.silverminer.color_block.util.Config;
import com.silverminer.color_block.util.network.ColorBlockPacketHandler;
import com.silverminer.color_block.util.saves.ColorBlocksSavedData;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {

	protected static final Logger LOGGER = LogManager.getLogger(CommonEvents.class);

	@Mod.EventBusSubscriber(modid = ColorBlockMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEventBus {
		/**
		 * This Event is used to register Rendertypes of blocks or Screens
		 * 
		 * @param event
		 */
		@SubscribeEvent
		public static void setupClient(FMLClientSetupEvent event) {
			LOGGER.info("Registering Renderers and Screens");

			ScreenManager.registerFactory(InitContainerType.COLOR_BLOCK.get(), ColorBlockScreen::new);
			ScreenManager.registerFactory(InitContainerType.IMAGE_CONTAINER.get(), ImageScreen::new);
		}

		@SubscribeEvent
		public static void setupCommon(FMLCommonSetupEvent event) {
			LOGGER.info("Registering Packets with Version: {}", ColorBlockPacketHandler.PROTOCOL_VERSION);

			ColorBlockPacketHandler.register();
		}
	}

	@Mod.EventBusSubscriber(modid = ColorBlockMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeEventBus {
		/**
		 * The onWorldLoad event is used to call the read-in
		 * method({@link ColorBlocksSavedData#get(ServerWorld)}) of
		 * {@link ColorBlocksSavedData}
		 * 
		 * @param event
		 */
		@SubscribeEvent
		public static void onWorldLoad(WorldEvent.Load event) {

			if (event.getWorld().isRemote()) {
				if (!ColorBlockMod.image_path.exists()) {
					ColorBlockMod.image_path.mkdir();
				}
				Config.readImageConfig();
			}

			if (event.getWorld() instanceof ServerWorld) {
				World world = ((ServerWorld) event.getWorld()).getWorld();

				// WorldSavedData can no longer be stored per map but only per dimension. So
				// store the registry in the overworld.
				if (!world.isRemote() && world.func_234923_W_() == World.field_234918_g_
						&& world instanceof ServerWorld) {
					ColorBlockMod.colorBlocksSavedData = ColorBlocksSavedData.get((ServerWorld) world);
				}
			}
		}
	}
}