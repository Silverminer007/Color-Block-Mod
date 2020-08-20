package com.silverminer.color_block.util.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.gui.screen.ImageScreen;
import com.silverminer.color_block.util.Config;
import com.silverminer.color_block.util.saves.ColorBlocksSavedData;

import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ColorBlockMod.MODID)
public class CommonEvents {

	protected static final Logger LOGGER = LogManager.getLogger(CommonEvents.class);

	/**
	 * The onWorldLoad event is used to call the read-in
	 * method({@link ColorBlocksSavedData#get(ServerWorld)}) of
	 * {@link ColorBlocksSavedData}
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {

		if (!ImageScreen.image_path.exists()) {
			ImageScreen.image_path.mkdir();
		}

		Config.readImageConfig();

		if (event.getWorld() instanceof ServerWorld) {
			World world = ((ServerWorld) event.getWorld()).getWorld();

			// WorldSavedData can no longer be stored per map but only per dimension. So
			// store the registry in the overworld.
			if (!world.isRemote() && world.func_234923_W_() == World.field_234918_g_
					&& world instanceof ServerWorld) {
				ColorBlockMod.colorBlocksSavedData = ColorBlocksSavedData.get((ServerWorld) world);
				LOGGER.info("Read in ColorBlockSavedData from World: {}", world.func_230315_m_());
			}
		}
	}
}