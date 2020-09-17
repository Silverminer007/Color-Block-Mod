package com.silverminer.color_block.util.saves;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.util.math.NumberingSystem;
import com.silverminer.color_block.util.network.ColorBlockPacketHandler;
import com.silverminer.color_block.util.network.SavesChangePacket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public final class Saves {

	public static final HashMap<UUID, PlayerSaves> SAVES_BY_PLAYER_UUID = new HashMap<UUID, PlayerSaves>();

	public static PlayerSaves getSaves(UUID uuid) {
		return Saves.SAVES_BY_PLAYER_UUID.get(uuid);
	}

	public static PlayerSaves getSaves(PlayerEntity player) {
		return Saves.getSaves(player.getUniqueID());
	}

	/**
	 * 
	 * @param uuid       of the Player
	 * @param saves      The Things that should be saved
	 * @param sendPacket Only type true if game is on Server. Otherwise the game
	 *                   will crash. If true an Packet will be send to the Server to
	 *                   sync Server and Client
	 */
	public static void setOrCreateSaves(@Nonnull UUID uuid, @Nonnull PlayerSaves saves, boolean sendPacket) {
		if (Saves.SAVES_BY_PLAYER_UUID.containsKey(uuid)) {
			Saves.SAVES_BY_PLAYER_UUID.remove(uuid);
		}
		Saves.SAVES_BY_PLAYER_UUID.putIfAbsent(uuid, saves);
		if (ColorBlockMod.colorBlocksSavedData != null) {
			ColorBlockMod.colorBlocksSavedData.markDirty();
		}
		if (sendPacket) {
			ColorBlockPacketHandler.sendToServer(new SavesChangePacket(saves));
		}
	}

	/**
	 * 
	 * @param uuid  of the Player
	 * @param saves The Things that should be saved
	 */
	public static void setOrCreateSaves(@Nonnull PlayerEntity player, @Nonnull PlayerSaves saves) {
		Saves.setOrCreateSaves(player.getUniqueID(), saves, false);
	}

	/**
	 * 
	 * @param uuid       of the Player
	 * @param saves      The Things that should be saved
	 * @param sendPacket Only type true if game is on Server. Otherwise the game
	 *                   will crash. If true an Packet will be send to the Server to
	 *                   sync Server and Client
	 */
	public static void setOrCreateSaves(@Nonnull PlayerEntity player, @Nonnull PlayerSaves saves, boolean sendPacket) {
		Saves.setOrCreateSaves(player.getUniqueID(), saves, sendPacket);
	}

	/**
	 * 
	 * @param uuid  of the Player
	 * @param saves The Things that should be saved
	 */
	public static void setOrCreateSaves(@Nonnull UUID uuid, @Nonnull PlayerSaves saves) {
		Saves.setOrCreateSaves(uuid, saves, false);
	}

	public static boolean removeSaves(UUID uuid) {
		if (Saves.SAVES_BY_PLAYER_UUID.containsKey(uuid)) {
			Saves.SAVES_BY_PLAYER_UUID.remove(uuid);
			if (ColorBlockMod.colorBlocksSavedData != null) {
				ColorBlockMod.colorBlocksSavedData.markDirty();
			}
			return true;
		}
		return false;
	}

	public static boolean removeSaves(PlayerEntity player) {
		return Saves.removeSaves(player.getUniqueID());
	}

	public static CompoundNBT serializeSystem(CompoundNBT nbt) {
		int i = 0;
		Iterator<UUID> iterator = SAVES_BY_PLAYER_UUID.keySet().iterator();
		while (iterator.hasNext()) {
			UUID uuid = iterator.next();
			if (SAVES_BY_PLAYER_UUID.containsKey(uuid)) {
				PlayerSaves saves = SAVES_BY_PLAYER_UUID.get(uuid);
				CompoundNBT tag = new CompoundNBT();
				tag.putUniqueId("uuid", uuid);
				tag.putInt("base", saves.getSystem().getBase());
				tag.putInt("max_image_x", saves.getMaxImageX());
				tag.putInt("max_image_y", saves.getMaxImageY());
				tag.putBoolean("fill_empty_pixel", saves.fillEmptyFixel());
				tag.putBoolean("ignore_image_size", saves.ignoreImageSize());
				tag.putInt("color_to_fill", saves.getColorToFill());
				nbt.put(String.valueOf(i), tag);
				i++;
			}
		}
		SAVES_BY_PLAYER_UUID.clear();
		return nbt;
	}

	/**
	 * Lieﬂt dieses Element aus CompountNBT aus
	 * 
	 * @param nbt Das Tag aus dem Ausgelesen wird
	 */
	public static void deserializeNBT(CompoundNBT nbt) {
		SAVES_BY_PLAYER_UUID.clear();
		int i = 0;
		while (nbt.contains(String.valueOf(i))) {
			CompoundNBT tag = nbt.getCompound(String.valueOf(i));
			if (!SAVES_BY_PLAYER_UUID.containsKey(tag.getUniqueId("uuid"))) {
				PlayerSaves saves = new PlayerSaves();
				if (tag.contains("base", 3)) {
					saves.setSystem(NumberingSystem.getByBase(tag.getInt("base")));
				}
				if (tag.contains("max_image_x", 3)) {
					saves.setMaxImageX(tag.getInt("max_image_x"));
				}
				if (tag.contains("max_image_y", 3)) {
					saves.setMaxImageY(tag.getInt("max_image_y"));
				}
				if (tag.contains("fill_empty_pixel", 1)) {
					saves.setFillEmtpyPixel(tag.getBoolean("fill_empty_pixel"));
				}
				if (tag.contains("ignore_image_size", 1)) {
					saves.setIgnoreImageSize(tag.getBoolean("ignore_image_size"));
				}
				if (tag.contains("color_to_fill", 3)) {
					saves.setColorToFill(tag.getInt("color_to_fill"));
				}
				SAVES_BY_PLAYER_UUID.put(tag.getUniqueId("uuid"), saves);
			}
			i++;// Next Element
		}
	}
}