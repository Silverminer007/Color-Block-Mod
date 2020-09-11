package com.silverminer.color_block.objects.tile_entity;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.gui.container.ColorBlockContainer;
import com.silverminer.color_block.init.InitTileEntityTypes;
import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ColorBlockTileEntity extends TileEntity implements INamedContainerProvider {

	protected static final Logger LOGGER = LogManager.getLogger(ColorBlockTileEntity.class);

	private int color = 0xffffff;

	public ColorBlockTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public ColorBlockTileEntity() {
		this(InitTileEntityTypes.COLOR_BLOCK.get());
	}

	public ColorBlockTileEntity setColor(int color) {
		this.color = color;
		this.markDirty();
		return this;
	}

	public int getBlockColor() {
		return this.color;
	}

	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}

	public CompoundNBT write(CompoundNBT nbt) {
		nbt.putInt("color", ColorBlock.getColorStatic(this.getPos(), this.getWorld()));
		return super.write(nbt);
	}

	public void func_230337_a_(BlockState state, CompoundNBT nbt) {
		super.func_230337_a_(state, nbt);
		if (nbt.contains("color", 3)) {
			ColorBlock.setColorStatic(nbt.getInt("color"), this);
		}
	}

	/**
	 * Retrieves packet to send to the client whenever this Tile Entity is resynced
	 * via World.notifyBlockUpdate. For modded TE's, this packet comes back to you
	 * clientside in {@link #onDataPacket}
	 */
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, -1, this.getUpdateTag());
	}

	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.func_230337_a_(null, pkt.getNbtCompound());
	}

	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
		return new ColorBlockContainer(id, playerInventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.color_block");
	}

	public String toString() {
		return "ColorBlockTileEntity{Color:" + this.color + ", World:" + this.getWorld().toString() + ", Position:"
				+ this.getPos() + "}";
	}
}