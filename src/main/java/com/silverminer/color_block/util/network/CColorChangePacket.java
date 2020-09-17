package com.silverminer.color_block.util.network;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * This Class is named with 'C' as prefix, because it's send from the CLIENT to
 * the Server
 * 
 * Dieses Packet wird immer von einem Clienten an den Server gesendet wenn sich
 * in der GUI die Farbe verändert hat. In der "Handle" Klasse wird dann auf der
 * Server Seite das Packet ausgewerten und auch dort die Frabe geändert
 * 
 * @author Silverminer007
 *
 */
public class CColorChangePacket {

	protected static final Logger LOGGER = LogManager.getLogger(CColorChangePacket.class);

	private final int color;
	private final BlockPos position;

	private final boolean setBlock;

	public CColorChangePacket(int colorIn, BlockPos pos, boolean setBlock) {
		this.position = pos;
		this.color = colorIn;
		this.setBlock = setBlock;
	}

	public static void encode(CColorChangePacket pkt, PacketBuffer buf) {
		buf.writeInt(pkt.color);
		buf.writeBlockPos(pkt.position);
		buf.writeBoolean(pkt.setBlock);
	}

	public static CColorChangePacket decode(PacketBuffer buf) {
		return new CColorChangePacket(buf.readInt(), buf.readBlockPos(), buf.readBoolean());
	}

	public static void handle(CColorChangePacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		// Aus irgendeinem Grund kommt das Server Packet trotzdem auf der Client Seite
		// an. Da context.getSender() aber einen ServerPlayerENtity zurück gbt hat man
		// trotzdem die Server Welt
		// context.enqueueWork wird benutzt um die änderungen im Main Thread und nicht
		// im Network Thread zu übernehmen
		context.enqueueWork(Handle.handle(context.getSender(), pkt.color, pkt.position, pkt.setBlock));
		context.setPacketHandled(true);
	}

	public static class Handle {
		public static DistExecutor.SafeRunnable handle(ServerPlayerEntity playerEntity, int color, BlockPos pos,
				boolean setBlock) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 2L;

				@Override
				public void run() {
					// Hier werden einfach die änderungen wie immer übernommen nur das die Welt eine
					// Server Welt ist
					if (playerEntity == null)
						return;
					World world = playerEntity.getEntityWorld();
					if (!(color < 0)) {
						world.setBlockState(pos, InitBlocks.COLOR_BLOCK.get().getDefaultState());

						ColorBlock.setColorStatic(color, pos, world);

						// Lässt Minecraft diesen Block neu rendern um die Farb änderung zu übernehmen
						BlockState state = world.getBlockState(pos);
						world.setBlockState(pos, state, 64);
						world.notifyBlockUpdate(pos, state, state, 0);
						world.markBlockRangeForRenderUpdate(pos, state, state);
					} else {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
					}
				}
			};
		}
	}
}