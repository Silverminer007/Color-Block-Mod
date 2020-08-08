package com.silverminer.color_block.util.saves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.silverminer.color_block.util.math.NumberingSystem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public final class ColorBlockContainerSaves {
	public static final HashMap<UUID, BlockPos> BLOCKS_BY_PLAYER_UUID = new HashMap<UUID, BlockPos>();

	public static final HashMap<UUID, NumberingSystem> SYSTEM_BY_PLAYER_UUID = new HashMap<UUID, NumberingSystem>();

	public static final HashMap<UUID, Integer> COLOR_BY_UUID = new HashMap<UUID, Integer>();

	public static final ArrayList<UUID> UUIDs = new ArrayList<UUID>();

	public static BlockPos getPosition(UUID uuid) {
		return ColorBlockContainerSaves.BLOCKS_BY_PLAYER_UUID.get(uuid);
	}

	public static BlockPos getPosition(PlayerEntity player) {
		return ColorBlockContainerSaves.getPosition(player.getUniqueID());
	}

	public static void setOrCreatePos(@Nonnull UUID uuid, @Nonnull BlockPos pos) {
		if (ColorBlockContainerSaves.BLOCKS_BY_PLAYER_UUID.containsKey(uuid)) {
			ColorBlockContainerSaves.BLOCKS_BY_PLAYER_UUID.remove(uuid);
		}
		ColorBlockContainerSaves.BLOCKS_BY_PLAYER_UUID.putIfAbsent(uuid, pos);
		if (!UUIDs.contains(uuid)) {
			UUIDs.add(uuid);
		}
	}

	public static void setOrCreatePos(@Nonnull PlayerEntity player, @Nonnull BlockPos pos) {
		ColorBlockContainerSaves.setOrCreatePos(player.getUniqueID(), pos);
	}

	public static NumberingSystem getSystem(UUID uuid) {
		return ColorBlockContainerSaves.SYSTEM_BY_PLAYER_UUID.get(uuid);
	}

	public static NumberingSystem getSystem(PlayerEntity player) {
		return ColorBlockContainerSaves.getSystem(player.getUniqueID());
	}

	public static void setOrCreateSystem(@Nonnull UUID uuid, @Nonnull NumberingSystem system) {
		if (ColorBlockContainerSaves.SYSTEM_BY_PLAYER_UUID.containsKey(uuid)) {
			ColorBlockContainerSaves.SYSTEM_BY_PLAYER_UUID.remove(uuid);
		}
		ColorBlockContainerSaves.SYSTEM_BY_PLAYER_UUID.putIfAbsent(uuid, system);
		if (!UUIDs.contains(uuid)) {
			UUIDs.add(uuid);
		}
	}

	public static void setOrCreateSystem(@Nonnull PlayerEntity player, @Nonnull NumberingSystem system) {
		ColorBlockContainerSaves.setOrCreateSystem(player.getUniqueID(), system);
	}


	public static Integer getColor(UUID uuid) {
		return ColorBlockContainerSaves.COLOR_BY_UUID.get(uuid);
	}

	public static Integer getColor(PlayerEntity player) {
		return ColorBlockContainerSaves.COLOR_BY_UUID.get(player.getUniqueID());
	}

	public static void setOrCreateColor(@Nonnull UUID uuid, @Nonnull Integer color) {
		if (ColorBlockContainerSaves.COLOR_BY_UUID.containsKey(uuid)) {
			ColorBlockContainerSaves.COLOR_BY_UUID.remove(uuid);
		}
		ColorBlockContainerSaves.COLOR_BY_UUID.putIfAbsent(uuid, color);
	}

	public static CompoundNBT serializeSystem(CompoundNBT nbt) {
		for (UUID uuid : UUIDs) {
			if (SYSTEM_BY_PLAYER_UUID.containsKey(uuid)) {
				CompoundNBT tag = new CompoundNBT();
				tag.putUniqueId("uuid", uuid);
				tag.putInt("base", SYSTEM_BY_PLAYER_UUID.get(uuid).getBase());
				nbt.put(String.valueOf(UUIDs.indexOf(uuid)), tag);
			}
		}
		UUIDs.clear();
		SYSTEM_BY_PLAYER_UUID.clear();
		return nbt;
	}

	/**
	 * Lieﬂt dieses Element aus CompountNBT aus
	 * 
	 * @param nbt Das Tag aus dem Ausgelesen wird
	 */
	public static void deserializeNBT(CompoundNBT nbt) {
		UUIDs.clear();
		SYSTEM_BY_PLAYER_UUID.clear();
		int i = 0;
		while (nbt.contains(String.valueOf(i))) {
			CompoundNBT tag = nbt.getCompound(String.valueOf(i));
			if (!SYSTEM_BY_PLAYER_UUID.containsKey(tag.getUniqueId("uuid"))) {
				SYSTEM_BY_PLAYER_UUID.put(tag.getUniqueId("uuid"), NumberingSystem.getByBase(tag.getInt("base")));
			}
			if (!UUIDs.contains(tag.getUniqueId("uuid"))) {
				UUIDs.add(tag.getUniqueId("uuid"));
			}
			i++;// Next Element
		}
	}
}