package com.silverminer.color_block.util.network;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * This Class is named with 'S' as prefix, because it's send from the SERVER to the Client
 * 
 * @author Silverminer007
 *
 */
public class SColorChangePacket {

	protected static final Logger LOGGER = LogManager.getLogger(SColorChangePacket.class);

	private final int color;
	private final BlockPos position;

	private final int playerID;

	public SColorChangePacket(int colorIn, BlockPos pos, int playerID) {
		this.playerID = playerID;
		this.position = pos;
		this.color = colorIn;
	}

	public static void encode(SColorChangePacket pkt, PacketBuffer buf) {
		buf.writeInt(pkt.color);
		buf.writeBlockPos(pkt.position);
		buf.writeInt(pkt.playerID);
	}

	public static SColorChangePacket decode(PacketBuffer buf) {
		return new SColorChangePacket(buf.readInt(), buf.readBlockPos(), buf.readInt());
	}

	public static void handle(SColorChangePacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT,
				() -> Handle.handleClient(pkt.playerID, pkt.color, pkt.position)));
		context.setPacketHandled(true);
	}

	public static class Handle {

		public static DistExecutor.SafeRunnable handleClient(int playerID, int color, BlockPos pos) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					@SuppressWarnings("resource")
					Entity playerEntity = Minecraft.getInstance().world.getEntityByID(playerID);
					if(playerEntity == null) return;
					World world = playerEntity.getEntityWorld();

					ColorBlock.setColorStatic(color, pos, world);

					// Lässt Minecraft diesen Block neu rendern um die Farb änderung zu übernehmen
					BlockState state = world.getBlockState(pos);
					world.setBlockState(pos, state, 64);
					world.notifyBlockUpdate(pos, state, state, 0);
					world.markBlockRangeForRenderUpdate(pos, state, state);
				}
			};
		}
	}
}