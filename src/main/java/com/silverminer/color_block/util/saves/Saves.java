package com.silverminer.color_block.util.saves;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.silverminer.color_block.util.math.NumberingSystem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public final class Saves {
	public static final HashMap<UUID, BlockPos> BLOCKS_BY_PLAYER_UUID = new HashMap<UUID, BlockPos>();

	public static final HashMap<UUID, NumberingSystem> SYSTEM_BY_PLAYER_UUID = new HashMap<UUID, NumberingSystem>();

	public static final HashMap<UUID, Integer> COLOR_BY_UUID = new HashMap<UUID, Integer>();

	public static final HashMap<UUID, ImageTransferPacket> IMAGE_BY_UUID = new HashMap<UUID, ImageTransferPacket>();

	public static BlockPos getPosition(UUID uuid) {
		return Saves.BLOCKS_BY_PLAYER_UUID.get(uuid);
	}

	public static BlockPos getPosition(PlayerEntity player) {
		return Saves.getPosition(player.getUniqueID());
	}

	public static void setOrCreatePos(@Nonnull UUID uuid, @Nonnull BlockPos pos) {
		if (Saves.BLOCKS_BY_PLAYER_UUID.containsKey(uuid)) {
			Saves.BLOCKS_BY_PLAYER_UUID.remove(uuid);
		}
		Saves.BLOCKS_BY_PLAYER_UUID.putIfAbsent(uuid, pos);
	}

	public static void setOrCreatePos(@Nonnull PlayerEntity player, @Nonnull BlockPos pos) {
		Saves.setOrCreatePos(player.getUniqueID(), pos);
	}

	public static boolean removePos(UUID uuid) {
		if (Saves.BLOCKS_BY_PLAYER_UUID.containsKey(uuid)) {
			Saves.BLOCKS_BY_PLAYER_UUID.remove(uuid);
			return true;
		}
		return false;
	}

	public static boolean removePos(PlayerEntity player) {
		return Saves.removePos(player.getUniqueID());
	}

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
	}

	public static void setOrCreateSystem(@Nonnull PlayerEntity player, @Nonnull NumberingSystem system) {
		Saves.setOrCreateSystem(player.getUniqueID(), system);
	}

	public static boolean removeSystem(UUID uuid) {
		if (Saves.SYSTEM_BY_PLAYER_UUID.containsKey(uuid)) {
			Saves.SYSTEM_BY_PLAYER_UUID.remove(uuid);
			return true;
		}
		return false;
	}

	public static boolean removeSystem(PlayerEntity player) {
		return Saves.removeSystem(player.getUniqueID());
	}

	public static Integer getColor(UUID uuid) {
		return Saves.COLOR_BY_UUID.get(uuid);
	}

	public static Integer getColor(PlayerEntity player) {
		return Saves.COLOR_BY_UUID.get(player.getUniqueID());
	}

	public static void setOrCreateColor(@Nonnull UUID uuid, @Nonnull Integer color) {
		if (Saves.COLOR_BY_UUID.containsKey(uuid)) {
			Saves.COLOR_BY_UUID.remove(uuid);
		}
		Saves.COLOR_BY_UUID.putIfAbsent(uuid, color);
	}

	public static boolean removeColor(UUID uuid) {
		if (Saves.COLOR_BY_UUID.containsKey(uuid)) {
			Saves.COLOR_BY_UUID.remove(uuid);
			return true;
		}
		return false;
	}

	public static boolean removeColor(PlayerEntity player) {
		return Saves.removeColor(player.getUniqueID());
	}

	public static ImageTransferPacket getImage(UUID uuid) {
		return Saves.IMAGE_BY_UUID.get(uuid);
	}

	public static ImageTransferPacket getImage(PlayerEntity player) {
		return Saves.getImage(player.getUniqueID());
	}

	public static ImageTransferPacket getImageOrDefault(PlayerEntity player, ImageTransferPacket defPacket) {
		ImageTransferPacket image = Saves.getImage(player.getUniqueID());
		return image == null ? defPacket : image;
	}

	public static void setOrCreateImage(@Nonnull UUID uuid, @Nonnull ImageTransferPacket image) {
		if (Saves.IMAGE_BY_UUID.containsKey(uuid)) {
			Saves.IMAGE_BY_UUID.remove(uuid);
		}
		Saves.IMAGE_BY_UUID.putIfAbsent(uuid, image);
	}

	public static boolean removeImage(UUID uuid) {
		if (Saves.IMAGE_BY_UUID.containsKey(uuid)) {
			Saves.IMAGE_BY_UUID.remove(uuid);
			return true;
		}
		return false;
	}

	public static boolean removeImage(PlayerEntity player) {
		return Saves.removeImage(player.getUniqueID());
	}

	public static CompoundNBT serializeSystem(CompoundNBT nbt) {
		Set<UUID> uuids = SYSTEM_BY_PLAYER_UUID.keySet();
		for (int i = 0; i < uuids.size(); i++) {
			UUID uuid = (UUID) uuids.toArray()[i];
			if (SYSTEM_BY_PLAYER_UUID.containsKey(uuid)) {
				CompoundNBT tag = new CompoundNBT();
				tag.putUniqueId("uuid", uuid);
				tag.putInt("base", SYSTEM_BY_PLAYER_UUID.get(uuid).getBase());
				nbt.put(String.valueOf(i), tag);
			}
		}
		SYSTEM_BY_PLAYER_UUID.clear();
		return nbt;
	}

	/**
	 * Ließt dieses Element aus CompountNBT aus
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