package com.silverminer.color_block.util.network;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * This Class is named with 'C' as prefix, because it's send from the CLIENT to
 * the Server
 * 
 * @author Silverminer007
 *
 */
public class CColorChangePacket {

	protected static final Logger LOGGER = LogManager.getLogger(CColorChangePacket.class);

	private final int color;
	private final BlockPos position;

	public CColorChangePacket(int colorIn, BlockPos pos) {
		this.position = pos;
		this.color = colorIn;
	}

	public static void encode(CColorChangePacket pkt, PacketBuffer buf) {
		buf.writeInt(pkt.color);
		buf.writeBlockPos(pkt.position);
	}

	public static CColorChangePacket decode(PacketBuffer buf) {
		return new CColorChangePacket(buf.readInt(), buf.readBlockPos());
	}

	public static void handle(CColorChangePacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT,
				() -> Handle.handleServer(context.getSender(), pkt.color, pkt.position)));
		context.setPacketHandled(true);
	}

	public static class Handle {
		public static DistExecutor.SafeRunnable handleServer(ServerPlayerEntity playerEntity, int color, BlockPos pos) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 2L;

				@Override
				public void run() {

					if (playerEntity == null)
						return;
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