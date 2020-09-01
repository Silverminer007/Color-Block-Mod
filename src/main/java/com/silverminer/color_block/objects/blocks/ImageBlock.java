package com.silverminer.color_block.objects.blocks;

import com.silverminer.color_block.init.InitTileEntityTypes;
import com.silverminer.color_block.objects.tile_entity.ImageTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ImageBlock extends Block {

	public ImageBlock(Properties properties) {
		super(properties);
	}

	public TileEntity createTileEntity(BlockState state, IBlockReader worldIn) {
		return InitTileEntityTypes.IMAGE_BLOCK.get().create();
	}

	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if (player.isCreative()) {
			if (!worldIn.isRemote()) {
				TileEntity tile = worldIn.getTileEntity(pos);
				if (tile instanceof ImageTileEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, (ImageTileEntity) tile, pos);
					return ActionResultType.SUCCESS;
				}
			}
		}
		return ActionResultType.PASS;
	}
}