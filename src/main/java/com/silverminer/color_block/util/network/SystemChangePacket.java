package com.silverminer.color_block.util.network;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.util.math.NumberingSystem;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class SystemChangePacket {
	protected static final Logger LOGGER = LogManager.getLogger(SystemChangePacket.class);

	private final int numbering_system_base;

	public SystemChangePacket(int numbering_system_base) {
		this.numbering_system_base = numbering_system_base;
	}

	public static void encode(SystemChangePacket pkt, PacketBuffer buf) {
		buf.writeInt(pkt.numbering_system_base);
	}

	public static SystemChangePacket decode(PacketBuffer buf) {
		return new SystemChangePacket(buf.readInt());
	}

	public static void handle(SystemChangePacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(Handle.handle(context.getSender(), pkt.numbering_system_base));
		context.setPacketHandled(true);
	}

	public static class Handle {

		public static DistExecutor.SafeRunnable handle(ServerPlayerEntity player, int numbering_system_base) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					Saves.setOrCreateSystem(player, NumberingSystem.getByBase(numbering_system_base));
				}
			};
		}
	}
}