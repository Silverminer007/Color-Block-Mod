package com.silverminer.color_block.objects.blocks;

import javax.annotation.Nullable;

import com.silverminer.color_block.gui.container.ImageContainer;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ImageBlock extends Block {

	public ImageBlock(Properties properties) {
		super(properties);
	}

	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if (player.isCreative()) {
			Saves.setOrCreatePos(player, pos);
			if (!worldIn.isRemote()) {
				player.openContainer(state.getContainer(worldIn, pos));
			}
			return ActionResultType.SUCCESS;
		} else {
			return ActionResultType.PASS;
		}
	}

	@Nullable
	public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
		return new SimpleNamedContainerProvider((windowId, playerInventory, player) -> {
			return new ImageContainer(windowId, playerInventory, IWorldPosCallable.of(worldIn, pos));
		}, new TranslationTextComponent("container.image_block"));
	}
}