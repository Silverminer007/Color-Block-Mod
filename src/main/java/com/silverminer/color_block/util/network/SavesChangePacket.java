package com.silverminer.color_block.util.network;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.util.math.NumberingSystem;
import com.silverminer.color_block.util.saves.PlayerSaves;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class SavesChangePacket {
	protected static final Logger LOGGER = LogManager.getLogger(SavesChangePacket.class);

	private final int numbering_system_base;

	private final int max_image_x, max_image_y;

	private final boolean ignore_image_size, fill_empty_pixel;

	private final int color_to_fill;

	public SavesChangePacket(int numbering_system_base, int max_image_x, int max_image_y, boolean ignore_image_size,
			boolean fill_empty_pixel, int color_to_fill) {
		this.numbering_system_base = numbering_system_base;
		this.max_image_x = max_image_x;
		this.max_image_y = max_image_y;
		this.ignore_image_size = ignore_image_size;
		this.fill_empty_pixel = fill_empty_pixel;
		this.color_to_fill = color_to_fill;
	}

	public SavesChangePacket(PlayerSaves saves) {
		this(saves.getSystem().getBase(), saves.getMaxImageX(), saves.getMaxImageY(), saves.ignoreImageSize(),
				saves.fillEmptyFixel(), saves.getColorToFill());
	}

	public static void encode(SavesChangePacket pkt, PacketBuffer buf) {
		buf.writeInt(pkt.numbering_system_base);
		buf.writeInt(pkt.max_image_x);
		buf.writeInt(pkt.max_image_y);
		buf.writeBoolean(pkt.ignore_image_size);
		buf.writeBoolean(pkt.fill_empty_pixel);
		buf.writeInt(pkt.color_to_fill);
	}

	public static SavesChangePacket decode(PacketBuffer buf) {
		return new SavesChangePacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean(),
				buf.readInt());
	}

	public static void handle(SavesChangePacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(Handle.handle(context.getSender(), pkt.numbering_system_base, pkt.max_image_x,
				pkt.max_image_y, pkt.ignore_image_size, pkt.fill_empty_pixel, pkt.color_to_fill));
		context.setPacketHandled(true);
	}

	public static class Handle {

		public static DistExecutor.SafeRunnable handle(ServerPlayerEntity player, int numbering_system_base,
				int max_image_x, int max_image_y, boolean ignore_image_size, boolean fill_empty_pixel,
				int color_to_fill) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					PlayerSaves saves = Saves.getSaves(player);
					saves = saves.setSystem(NumberingSystem.getByBase(numbering_system_base));
					saves = saves.setMaxImageX(max_image_x);
					saves = saves.setMaxImageY(max_image_y);
					saves = saves.setIgnoreImageSize(ignore_image_size);
					saves = saves.setFillEmtpyPixel(fill_empty_pixel);
					saves = saves.setColorToFill(color_to_fill);
					Saves.setOrCreateSaves(player, saves, false);
				}
			};
		}
	}
}