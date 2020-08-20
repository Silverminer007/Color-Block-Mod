package com.silverminer.color_block.util.saves;

import java.io.File;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class ImageTransferPacket {
	protected File file = new File("");
	protected int xOffset = 1;
	protected int yOffset = 0;

	protected int zOffset = 1;
	protected Rotation rot = Rotation.NONE;
	protected Axis axis = Axis.Y;

	public ImageTransferPacket() {
	}

	/**
	 * Type null for default
	 * 
	 * @param fileIn
	 * @param xOffsetIn
	 * @param yOffsetIn
	 * @param rotationIn
	 * @param axisIn
	 */
	public ImageTransferPacket(File fileIn, int xOffsetIn, int yOffsetIn, int zOffsetIn, Rotation rotationIn,
			Axis axisIn) {
		this.file = fileIn == null ? this.file : fileIn;
		this.xOffset = xOffsetIn == 0 ? this.xOffset : xOffsetIn;
		this.yOffset = yOffsetIn == 0 ? this.yOffset : yOffsetIn;
		this.zOffset = zOffsetIn == 0 ? this.zOffset : zOffsetIn;
		this.rot = rotationIn == null ? this.rot : rotationIn;
		this.axis = axisIn == null ? this.axis : axisIn;
	}

	public ImageTransferPacket setFile(File fileIn) {
		this.file = fileIn == null ? this.file : fileIn;
		return this;
	}

	public ImageTransferPacket setXOffset(Integer xOffsetIn) {
		this.xOffset = xOffsetIn == null ? this.xOffset : xOffsetIn;
		return this;
	}

	public ImageTransferPacket setYOffset(Integer yOffsetIn) {
		this.yOffset = yOffsetIn == null ? this.yOffset : yOffsetIn;
		return this;
	}

	public ImageTransferPacket setZOffset(Integer zOffsetIn) {
		this.zOffset = zOffsetIn == null ? this.zOffset : zOffsetIn;
		return this;
	}

	public ImageTransferPacket setRotation(Rotation rotationIn) {
		this.rot = rotationIn == null ? this.rot : rotationIn;
		return this;
	}

	public ImageTransferPacket setAxis(Axis axisIn) {
		this.axis = axisIn == null ? this.axis : axisIn;
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
		return "ImageTransferPacket{File:" + this.getFile().toString() + ", XOffset:" + this.getXOffset() + ", YOffset:"
				+ this.getYOffset() + ", ZOffset:" + this.getZOffset() + ", Rotation:" + this.getRotation().toString()
				+ ", Axis:" + this.getAxis().toString() + "}";
	}
}