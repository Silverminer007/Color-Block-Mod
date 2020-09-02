package com.silverminer.color_block.util.network;

import java.io.File;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.gui.container.ImageContainer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * This Class is named with 'S' as prefix, because it's send from the SERVER to
 * the Client
 * 
 * @author Silverminer007
 *
 */
public class ImageDataPacket {

	protected static final Logger LOGGER = LogManager.getLogger(ImageDataPacket.class);

	private final BlockPos blockPos;
	private final String file;
	private final BlockPos posOffset;

	private final String rotation;

	private final String axis;

	public ImageDataPacket(BlockPos blockPos, String file, BlockPos posOffset, String rotation, String axis) {
		this.blockPos = blockPos;
		this.posOffset = posOffset;
		this.file = file;
		this.axis = axis;
		this.rotation = rotation;
	}

	public static void encode(ImageDataPacket pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.blockPos);
		buf.writeString(pkt.file);
		buf.writeBlockPos(pkt.posOffset);
		buf.writeString(pkt.rotation);
		buf.writeString(pkt.axis);
	}

	public static ImageDataPacket decode(PacketBuffer buf) {
		return new ImageDataPacket(buf.readBlockPos(), buf.readString(), buf.readBlockPos(), buf.readString(),
				buf.readString());
	}

	public static void handle(ImageDataPacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handleClient(context.getSender(),
				pkt.blockPos, pkt.file, pkt.posOffset, pkt.rotation, pkt.axis)));
		context.setPacketHandled(true);
	}

	public static class Handle {

		public static DistExecutor.SafeRunnable handleClient(ServerPlayerEntity player, BlockPos blockPos, String file,
				BlockPos offsetPos, String rotation, String axis) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					LOGGER.info("Handeling Image Data Packet");
					Rotation rot;
					try {
						rot = Rotation.valueOf(rotation);
					} catch (IllegalArgumentException e) {
						rot = Rotation.NONE;
					}
					Axis axis2;
					try {
						axis2 = Axis.valueOf(axis);
					} catch (IllegalArgumentException e) {
						axis2 = Axis.Y;
					}
					ImageContainer.buildImage(player.getEntityWorld(), new File(file), blockPos, offsetPos, rot, axis2,
							player);
				}
			};
		}
	}
}