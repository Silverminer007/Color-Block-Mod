package com.silverminer.color_block.gui.container;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.init.InitContainerType;
import com.silverminer.color_block.objects.blocks.ColorBlock;
import com.silverminer.color_block.objects.tile_entity.ColorBlockTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ColorBlockContainer extends Container {

	protected static final Logger LOGGER = LogManager.getLogger(ColorBlockContainer.class);

	protected final IWorldPosCallable iWorld;
	protected final PlayerEntity player;

	protected final ColorBlockTileEntity tileEntity;

	public ColorBlockContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data) {
		this(windowId, playerInv, getTileEntity(playerInv, data));
	}

	public ColorBlockContainer(int id, PlayerInventory playerInventory, ColorBlockTileEntity colorBlockTileEntity) {
		super(InitContainerType.COLOR_BLOCK.get(), id);
		this.iWorld = IWorldPosCallable.of(colorBlockTileEntity.getWorld(), colorBlockTileEntity.getPos());
		this.player = playerInventory.player;
		this.tileEntity = colorBlockTileEntity;
	}

	public static ColorBlockTileEntity getTileEntity(PlayerInventory playerInventory, PacketBuffer data) {
		Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
		if (tileAtPos instanceof ColorBlockTileEntity) {
			return (ColorBlockTileEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	public boolean func_230302_a_(BlockState state) {
		return state.isIn(InitBlocks.COLOR_BLOCK.get());
	}

	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		BlockPos pos = this.tileEntity.getPos();
		World playerWorld = playerIn.getEntityWorld();
		BlockState state = playerWorld.getBlockState(pos);
		if (this.getColor() != -1) {
			ColorBlock.setColorStatic(this.getColor(), pos, playerWorld);
		}
		playerWorld.setBlockState(pos, state, 64);
		playerWorld.notifyBlockUpdate(pos, state, state, 64);
		playerWorld.markBlockRangeForRenderUpdate(pos, state, state);
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.iWorld.applyOrElse((world, pos) -> {
			return !this.func_230302_a_(world.getBlockState(pos)) ? false
					: playerIn.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
							(double) pos.getZ() + 0.5D) <= 64.0D;
		}, true);
	}

	public int getColor() {
		return ColorBlock.getColorStatic(this.tileEntity.getPos(), this.tileEntity.getWorld());
	}

	public ColorBlockTileEntity getTileEntity() {
		return this.tileEntity;
	}
}