package com.silverminer.color_block.util.network;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
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
public class ImageColorChangePacket {

	protected static final Logger LOGGER = LogManager.getLogger(ImageColorChangePacket.class);

	private final int color;
	private final BlockPos position;

	private final BlockState state;

	public ImageColorChangePacket(int colorIn, BlockPos pos, BlockState oldState) {
		this.position = pos;
		this.color = colorIn;
		this.state = oldState;
	}

	public ImageColorChangePacket(int colorIn, BlockPos pos, CompoundNBT oldState) {
		this(colorIn, pos, NBTUtil.readBlockState(oldState));
	}

	public static void encode(ImageColorChangePacket pkt, PacketBuffer buf) {
		buf.writeInt(pkt.color);
		buf.writeBlockPos(pkt.position);
		buf.writeCompoundTag(NBTUtil.writeBlockState(pkt.state));
	}

	public static ImageColorChangePacket decode(PacketBuffer buf) {
		return new ImageColorChangePacket(buf.readInt(), buf.readBlockPos(), buf.readCompoundTag());
	}

	public static void handle(ImageColorChangePacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		// Aus irgendeinem Grund kommt das Server Packet trotzdem auf der Client Seite
		// an. Da context.getSender() aber einen ServerPlayerEntity zurück gbt hat man
		// trotzdem die Server Welt
		// context.enqueueWork wird benutzt um die änderungen im Main Thread und nicht
		// im Network Thread zu übernehmen
		context.enqueueWork(Handle.handle(context.getSender(), pkt.color, pkt.position, pkt.state));
		context.setPacketHandled(true);
	}

	public static class Handle {
		public static DistExecutor.SafeRunnable handle(ServerPlayerEntity playerEntity, int color, BlockPos pos, BlockState oldState) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 2L;

				@Override
				public void run() {
					// Hier werden einfach die änderungen wie immer übernommen nur das die Welt eine
					// Server Welt ist
					if (playerEntity == null)
						return;
					World world = playerEntity.getEntityWorld();
					if (0 <= color) {
						world.setBlockState(pos, InitBlocks.COLOR_BLOCK.get().getDefaultState());

						ColorBlock.setColorStatic(color, pos, world);
						ColorBlock.setOverwrittenState(oldState, pos, world);

						// Lässt Minecraft diesen Block neu rendern um die Farb änderung zu übernehmen
						BlockState state = world.getBlockState(pos);
						world.setBlockState(pos, state, 64);
						world.notifyBlockUpdate(pos, state, state, 0);
						world.markBlockRangeForRenderUpdate(pos, state, state);
					} else if(-1 == color) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
					} else {
						world.setBlockState(pos, oldState);
					}
				}
			};
		}
	}
}