package com.silverminer.color_block.util.saves;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.util.math.NumberingSystem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public final class Saves {

	public static final HashMap<UUID, NumberingSystem> SYSTEM_BY_PLAYER_UUID = new HashMap<UUID, NumberingSystem>();

	public static NumberingSystem getSystem(UUID uuid) {
		return Saves.SYSTEM_BY_PLAYER_UUID.get(uuid);
	}

	public static NumberingSystem getSystem(PlayerEntity player) {
		return Saves.getSystem(player.getUniqueID());
	}

	public static void setOrCreateSystem(@Nonnull UUID uuid, @Nonnull NumberingSystem system) {
		if (Saves.SYSTEM_BY_PLAYER_UUID.containsKey(uuid)) {
			Saves.SYSTEM_BY_PLAYER_UUID.remove(uuid);
		}
		Saves.SYSTEM_BY_PLAYER_UUID.putIfAbsent(uuid, system);
		if (ColorBlockMod.colorBlocksSavedData != null) {
			ColorBlockMod.colorBlocksSavedData.markDirty();
		}
	}

	public static void setOrCreateSystem(@Nonnull PlayerEntity player, @Nonnull NumberingSystem system) {
		Saves.setOrCreateSystem(player.getUniqueID(), system);
	}

	public static boolean removeSystem(UUID uuid) {
		if (Saves.SYSTEM_BY_PLAYER_UUID.containsKey(uuid)) {
			Saves.SYSTEM_BY_PLAYER_UUID.remove(uuid);
			if (ColorBlockMod.colorBlocksSavedData != null) {
				ColorBlockMod.colorBlocksSavedData.markDirty();
			}
			return true;
		}
		return false;
	}

	public static boolean removeSystem(PlayerEntity player) {
		return Saves.removeSystem(player.getUniqueID());
	}

	public static CompoundNBT serializeSystem(CompoundNBT nbt) {
		int i = 0;
		Iterator<UUID> iterator = SYSTEM_BY_PLAYER_UUID.keySet().iterator();
		while (iterator.hasNext()) {
			UUID uuid = iterator.next();
			if (SYSTEM_BY_PLAYER_UUID.containsKey(uuid)) {
				CompoundNBT tag = new CompoundNBT();
				tag.putUniqueId("uuid", uuid);
				tag.putInt("base", SYSTEM_BY_PLAYER_UUID.get(uuid).getBase());
				nbt.put(String.valueOf(i), tag);
				i++;
			}
		}
		SYSTEM_BY_PLAYER_UUID.clear();
		return nbt;
	}

	/**
	 * Lieﬂt dieses Element aus CompountNBT aus
	 * 
	 * @param nbt Das Tag aus dem Ausgelesen wird
	 */
	public static void deserializeNBT(CompoundNBT nbt) {
		SYSTEM_BY_PLAYER_UUID.clear();
		int i = 0;
		while (nbt.contains(String.valueOf(i))) {
			CompoundNBT tag = nbt.getCompound(String.valueOf(i));
			if (!SYSTEM_BY_PLAYER_UUID.containsKey(tag.getUniqueId("uuid"))) {
				SYSTEM_BY_PLAYER_UUID.put(tag.getUniqueId("uuid"), NumberingSystem.getByBase(tag.getInt("base")));
			}
			i++;// Next Element
		}
	}
}