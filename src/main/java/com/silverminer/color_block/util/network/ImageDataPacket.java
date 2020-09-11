package com.silverminer.color_block.util.network;

import java.io.File;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.gui.container.ImageContainer;
import com.silverminer.color_block.objects.tile_entity.ColorBlockTileEntity;
import com.silverminer.color_block.objects.tile_entity.ImageTileEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * 
 * @author Silverminer007
 *
 */
public class ImageDataPacket {

	protected static final Logger LOGGER = LogManager.getLogger(ImageDataPacket.class);

	private final BlockPos blockPos;
	private final String file;
	private final BlockPos posOffset;

	private final int rotation;

	private final int axis;

	private final boolean buildImage;

	public ImageDataPacket(BlockPos blockPos, String file, BlockPos posOffset, int rotation, int axis,
			boolean buildImage) {
		this.blockPos = blockPos;
		this.posOffset = posOffset;
		this.file = file;
		this.axis = axis;
		this.rotation = rotation;
		this.buildImage = buildImage;
	}

	public static void encode(ImageDataPacket pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.blockPos);
		buf.writeString(pkt.file);
		buf.writeBlockPos(pkt.posOffset);
		buf.writeInt(pkt.rotation);
		buf.writeInt(pkt.axis);
		buf.writeBoolean(pkt.buildImage);
	}

	public static ImageDataPacket decode(PacketBuffer buf) {
		return new ImageDataPacket(buf.readBlockPos(), buf.readString(32767), buf.readBlockPos(), buf.readInt(),
				buf.readInt(), buf.readBoolean());
	}

	public static void handle(ImageDataPacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(Handle.handle(context.getSender(), pkt.blockPos, pkt.file, pkt.posOffset, pkt.rotation,
				pkt.axis, pkt.buildImage));
		context.setPacketHandled(true);
	}

	public static class Handle {

		public static DistExecutor.SafeRunnable handle(ServerPlayerEntity player, BlockPos blockPos, String file,
				BlockPos offsetPos, int rotation, int axis, boolean buildImage) {
			return new DistExecutor.SafeRunnable() {

				private static final long serialVersionUID = 9821743L;

				@Override
				public void run() {
					Rotation rot;
					try {
						rot = Rotation.values()[rotation];
					} catch (IllegalArgumentException e) {
						rot = Rotation.NONE;
					}
					Axis axis2;
					try {
						axis2 = Axis.values()[axis];
					} catch (IllegalArgumentException e) {
						axis2 = Axis.Y;
					}
					if (buildImage) {
						ImageContainer.buildImage(player.getEntityWorld(), new File(file), blockPos, offsetPos, rot,
								axis2, player);
					}

					TileEntity te = player.getEntityWorld().getTileEntity(blockPos);
					if (te instanceof ColorBlockTileEntity) {
						((ImageTileEntity) te).setAxis(axis2).setFile(new File(file)).setRotation(rot)
								.setXOffset(offsetPos.getX()).setZOffset(offsetPos.getZ()).setYOffset(offsetPos.getY());
						return;
					} else {
						return;
					}
				}
			};
		}
	}
}