package com.silverminer.color_block.util.saves;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.ColorBlockMod;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Responsible for saving/loading {@link ColorBlockSaveHelper} data.
 */
public class ColorBlocksSavedData extends WorldSavedData {
	private static final String DATA_NAME = ColorBlockMod.MODID;// Der Name der Datei in die gespeichert wird

	protected static final Logger LOGGER = LogManager.getLogger(ColorBlocksSavedData.class);

	public ColorBlocksSavedData() {
		super(DATA_NAME);
	}

	/**
	 * This Method is used to read in the COLOR_BLOCKS list from *.nbt file. Use
	 * {@link ColorBlocksSavedData#write(CompoundNBT)} to Save it
	 * 
	 * @param nbt This Parameter is the instance of the *.nbt file
	 */
	@Override
	public void read(CompoundNBT nbt) {
		Saves.deserializeNBT(nbt.getCompound("numbering_system"));
	}

	/**
	 * This Method is used to save the COLOR_BLOCKS list to *.nbt file use
	 * {@link ColorBlocksSavedData#read(CompoundNBT)} to read it from file
	 */
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt.put("numbering_system", Saves.serializeSystem(new CompoundNBT()));
		return nbt;
	}

	public static ColorBlocksSavedData get(ServerWorld world) {
		if (world == null)// return if it is null: You can't read from null: It catches error
			return null;
		LOGGER.info("ColorBlockSavedData read");
		DimensionSavedDataManager storage = world.getSavedData();// Get the Saved Data from world

		return storage.getOrCreate(ColorBlocksSavedData::new, DATA_NAME);// read the Data in and return it
	}
}