package com.silverminer.color_block.util.saves;

import com.silverminer.color_block.ColorBlockMod;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class ColorBlockSaveHelper implements INBTSerializable<CompoundNBT> {
	private BlockPos pos = BlockPos.ZERO;
	private int color = -1;
	private String dimension = "";

	public ColorBlockSaveHelper(BlockPos position, int color, String dimensionName) {
		this.pos = position;
		this.color = color;
		this.dimension = dimensionName;
	}

	public ColorBlockSaveHelper() {
	}

	public BlockPos getPosition() {
		return this.pos;
	}

	public int getColor() {
		return this.color;
	}

	public String getDimensionName() {
		return this.dimension;
	}

	public ColorBlockSaveHelper setPosition(BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public ColorBlockSaveHelper setColor(int color) {
		this.color = color;
		return this;
	}

	public ColorBlockSaveHelper setDimension(String dimension) {
		this.dimension = dimension;
		return this;
	}

	/**
	 * Serialisiert dieses element zu CompountNBT
	 */
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("color", this.getColor());
		tag.putString("dimension", this.getDimensionName());
		tag.putLong("position", this.getPosition().toLong());
		return tag;
	}

	/**
	 * Lieﬂt dieses Element aus CompountNBT aus
	 * 
	 * @param nbt Das Tag aus dem Ausgelesen wird
	 */
	public void deserializeNBT(CompoundNBT nbt) {
		BlockPos pos = BlockPos.fromLong(nbt.getLong("position"));
		int color = nbt.getInt("color");
		String dim = nbt.getString("dimension");
		ColorBlockSaveHelper helper = this.setColor(color).setDimension(dim).setPosition(pos);
		ColorBlockMod.COLOR_BLOCKS.add(helper);
	}

	public String toString() {
		return "ColorBlockSaveHelper{Color:" + this.getColor() + ", Position:" + this.getPosition().toString()
				+ ", Dimension:" + this.getDimensionName() + "}";
	}
}