package com.silverminer.color_block.util.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
	protected static final Logger LOGGER = LogManager.getLogger(ClientEvents.class);

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void renderBlockColor(ColorHandlerEvent.Block event) {
		LOGGER.debug("Rendering Block Color Colors");
		event.getBlockColors().register(new IBlockColor() {
			@Override
			public int getColor(BlockState state, IBlockDisplayReader displayReader, BlockPos pos, int tintindex) {
				return ColorBlock.getColorStatic(displayReader.getTileEntity(pos));
			}
		}, InitBlocks.COLOR_BLOCK.get());// The Block that it should be colored
	}
}