package com.silverminer.color_block.gui.container;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.init.InitContainerType;
import com.silverminer.color_block.objects.blocks.ColorBlock;
import com.silverminer.color_block.util.saves.ColorBlockContainerSaves;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ColorBlockContainer extends Container {

	protected static final Logger LOGGER = LogManager.getLogger(ColorBlockContainer.class);

	protected final IWorldPosCallable iWorld;
	protected final PlayerEntity player;

	public ColorBlockContainer(int windowId, PlayerInventory playerInventory, IWorldPosCallable iWorld) {
		super(InitContainerType.COLOR_BLOCK.get(), windowId);
		this.iWorld = iWorld;
		this.player = playerInventory.player;
	}

	public ColorBlockContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data) {
		this(windowId, playerInv, IWorldPosCallable.DUMMY);
	}

	public boolean func_230302_a_(BlockState state) {
		return state.isIn(InitBlocks.COLOR_BLOCK.get());
	}

	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		BlockPos pos = ColorBlockContainerSaves.getPosition(playerIn);
		World playerWorld = playerIn.getEntityWorld();
		BlockState state = playerWorld.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof ColorBlock) {
			ColorBlock colorBlock = (ColorBlock) block;
			if (this.getColor() != -1) {
				colorBlock.setColor(this.getColor(), pos);
			}
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

	public void setColor(int colorIn) {
		ColorBlockContainerSaves.setOrCreateColor(this.getPlayer().getUniqueID(), colorIn);
	}

	public int getColor() {
		Integer color = ColorBlockContainerSaves.getColor(this.getPlayer());
		return color != null ? color : -1;
	}

	public PlayerEntity getPlayer() {
		return this.player;
	}
}