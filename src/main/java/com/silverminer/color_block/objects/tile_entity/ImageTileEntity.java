package com.silverminer.color_block.objects.tile_entity;

import java.io.File;

import com.silverminer.color_block.gui.container.ImageContainer;
import com.silverminer.color_block.init.InitTileEntityTypes;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Rotation;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ImageTileEntity extends TileEntity implements INamedContainerProvider {

	protected File file = new File("");
	protected int xOffset = 1;
	protected int yOffset = 0;

	protected int zOffset = 1;
	protected Rotation rot = Rotation.NONE;
	protected Axis axis = Axis.Y;

	public ImageTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public ImageTileEntity() {
		this(InitTileEntityTypes.IMAGE_BLOCK.get());
	}

	public ImageTileEntity setFile(File fileIn) {
		this.file = fileIn == null ? this.file : fileIn;
		this.markDirty();
		return this;
	}

	public ImageTileEntity setXOffset(Integer xOffsetIn) {
		this.xOffset = xOffsetIn == null ? this.xOffset : xOffsetIn;
		this.markDirty();
		return this;
	}

	public ImageTileEntity setYOffset(Integer yOffsetIn) {
		this.yOffset = yOffsetIn == null ? this.yOffset : yOffsetIn;
		this.markDirty();
		return this;
	}

	public ImageTileEntity setZOffset(Integer zOffsetIn) {
		this.zOffset = zOffsetIn == null ? this.zOffset : zOffsetIn;
		this.markDirty();
		return this;
	}

	public ImageTileEntity setRotation(Rotation rotationIn) {
		this.rot = rotationIn == null ? this.rot : rotationIn;
		this.markDirty();
		return this;
	}

	public ImageTileEntity setAxis(Axis axisIn) {
		this.axis = axisIn == null ? this.axis : axisIn;
		this.markDirty();
		return this;
	}

	public File getFile() {
		return this.file;
	}

	public int getXOffset() {
		return this.xOffset;
	}

	public int getYOffset() {
		return this.yOffset;
	}

	public int getZOffset() {
		return this.zOffset;
	}

	public Rotation getRotation() {
		return this.rot;
	}

	public Axis getAxis() {
		return this.axis;
	}

	public BlockPos getOffsetPos() {
		return new BlockPos(this.getXOffset(), this.getYOffset(), this.getZOffset());
	}

	public String toString() {
		return "ImageTileEntity{File:" + this.getFile().toString() + ", XOffset:" + this.getXOffset() + ", YOffset:"
				+ this.getYOffset() + ", ZOffset:" + this.getZOffset() + ", Rotation:" + this.getRotation().toString()
				+ ", Axis:" + this.getAxis().toString() + "}";
	}

	public CompoundNBT getUpdateTag() {
		return this.write(super.getUpdateTag());
	}

	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		this.read(state, tag);
	}

	public CompoundNBT write(CompoundNBT nbt) {
		nbt.putString("file", this.file.toString());
		nbt.putLong("pos", this.getOffsetPos().toLong());
		nbt.putInt("rotation", this.rot.ordinal());
		nbt.putInt("axis", this.axis.ordinal());
		return super.write(nbt);
	}

	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if (nbt.contains("file", 8)) {
			this.setFile(new File(nbt.getString("file")));
		}
		if (nbt.contains("pos", 4)) {
			BlockPos pos = BlockPos.fromLong(nbt.getLong("pos"));
			this.setXOffset(pos.getX());
			this.setYOffset(pos.getY());
			this.setZOffset(pos.getZ());
		}
		if (nbt.contains("rotation", 3)) {
			try {
				this.rot = Rotation.values()[nbt.getInt("rotation")];
			} catch (IllegalArgumentException e) {
				this.rot = Rotation.NONE;
			}
		}
		if (nbt.contains("axis", 3)) {
			try {
				this.axis = Axis.values()[nbt.getInt("axis")];
			} catch (IllegalArgumentException e) {
				this.axis = Axis.Y;
			}
		}
	}

	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player) {
		return new ImageContainer(id, playerInv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.image_block");
	}
}