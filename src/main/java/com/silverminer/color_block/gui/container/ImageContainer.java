package com.silverminer.color_block.gui.container;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.init.InitContainerType;
import com.silverminer.color_block.objects.blocks.ColorBlock;
import com.silverminer.color_block.objects.tile_entity.ImageTileEntity;
import com.silverminer.color_block.util.network.ColorBlockPacketHandler;
import com.silverminer.color_block.util.network.ImageColorChangePacket;
import com.silverminer.color_block.util.network.ImageDataPacket;
import com.silverminer.color_block.util.saves.PlayerSaves;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ImageContainer extends Container {

	protected static final Logger LOGGER = LogManager.getLogger(ImageContainer.class);

	protected final IWorldPosCallable iWorld;
	protected final PlayerEntity player;

	protected final ImageTileEntity tileEntity;

	public ImageContainer(int windowId, PlayerInventory playerInventory, ImageTileEntity tileEntityIn) {
		super(InitContainerType.IMAGE_CONTAINER.get(), windowId);
		this.iWorld = IWorldPosCallable.of(tileEntityIn.getWorld(), tileEntityIn.getPos());
		this.player = playerInventory.player;
		this.tileEntity = tileEntityIn;
	}

	public ImageContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data) {
		this(windowId, playerInv, getTileEntity(playerInv, data));
	}

	public static ImageTileEntity getTileEntity(PlayerInventory playerInventory, PacketBuffer data) {
		Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final TileEntity tileAtPos = playerInventory.player.getEntityWorld().getTileEntity(data.readBlockPos());
		if (tileAtPos instanceof ImageTileEntity) {
			return (ImageTileEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	public boolean isRightBlock(BlockState state) {
		return state.isIn(InitBlocks.IMAGE_BLOCK.get());
	}

	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		if (playerIn.getEntityWorld().isRemote()) {
			// Send this to the Server to update the TileEntity Data there too. Else the
			// Data wouldn't be saved
			ColorBlockPacketHandler.sendToServer(new ImageDataPacket(this.getTileEntity().getPos(),
					this.getTileEntity().getFile().toString(), this.getTileEntity().getOffsetPos(),
					this.getTileEntity().getRotation().ordinal(), this.getTileEntity().getAxis().ordinal()));
		}
	}

	public void buildImage(PlayerEntity player) {
		ImageContainer.buildImage(this.getTileEntity().getWorld(), this.getTileEntity().getFile(),
				this.getTileEntity().getPos(), this.getTileEntity().getOffsetPos(), this.getTileEntity().getRotation(),
				this.getTileEntity().getAxis(), player);
	}

	public void removeImage(PlayerEntity player) {
		ImageContainer.removeImage(this.getTileEntity().getWorld(), this.getTileEntity().getFile(),
				this.getTileEntity().getPos(), this.getTileEntity().getOffsetPos(), this.getTileEntity().getRotation(),
				this.getTileEntity().getAxis(), player);
	}

	public static void buildImage(World world, File file, BlockPos pos, BlockPos offsetPos, Rotation rot, Axis axis,
			@Nullable PlayerEntity player) {
		try {
			file = file == null ? new File("") : file;
			if (file.exists() && !file.isDirectory()) {
				pos = pos.add(offsetPos);
				BufferedImage image = ImageIO.read(file);
				int imageX = image.getWidth(), imageY = image.getHeight();
				PlayerSaves saves = player == null ? new PlayerSaves() : Saves.getSaves(player);

				if ((imageX <= saves.getMaxImageX() && imageY <= saves.getMaxImageY()) || saves.ignoreImageSize()) {
					for (int x = 0; x < imageX; x++) {
						for (int y = 0; y < imageY; y++) {
							BlockPos position = BlockPos.ZERO;
							int posX = 0, posY = 0;
							switch (rot) {
							case NONE:
								posX = imageX - x;
								posY = imageY - y;
								break;
							case CLOCKWISE_90:
								posX = y;
								posY = imageX - x;
								break;
							case CLOCKWISE_180:
								posX = x;
								posY = y;
								break;
							case COUNTERCLOCKWISE_90:
								posX = imageY - y;
								posY = x;
								break;
							default:
								break;
							}
							switch (axis) {
							case Y:
								position = pos.add(posX, 0, posY);
								break;
							case X:
								position = pos.add(0, posY, posX);
								break;
							case Z:
								position = pos.add(posX, posY, 0);
								break;
							}

							int color = getColor(x, y, image, saves);
							BlockState oldState = world.getBlockState(position);
							if (!(color < 0)) {
								BlockState state = InitBlocks.COLOR_BLOCK.get().getDefaultState();
								world.setBlockState(position, state);
								if (state.getBlock().hasTileEntity(state)) {
									world.addTileEntity(state.getBlock().createTileEntity(state, world));
									ColorBlock.setColorStatic(color, position, world);
									ColorBlock.setOverwrittenState(oldState, position, world);
								}
								state = world.getBlockState(position);
								world.markBlockRangeForRenderUpdate(position, state, state);
								world.notifyBlockUpdate(position, state, state, 64);
							} else {
								world.setBlockState(position, Blocks.AIR.getDefaultState());
							}
							if (world.isRemote()) {
								ColorBlockPacketHandler
										.sendToServer(new ImageColorChangePacket(color, position, oldState));
							}
							continue;
						}
					}
				} else {
					if (world.isRemote() && player != null) {
						player.sendMessage(new TranslationTextComponent("container.image_block.to_big_image"),
								player.getUniqueID());
					}
				}
			} else {
				if (file != new File("") && player != null && world.isRemote()) {
					player.sendMessage(
							new TranslationTextComponent("container.image_block.file_non_exists", file.toString()),
							player.getUniqueID());
				}
			}
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public static void removeImage(World world, File file, BlockPos pos, BlockPos offsetPos, Rotation rot, Axis axis,
			@Nullable PlayerEntity player) {
		try {
			file = file == null ? new File("") : file;
			if (file.exists() && !file.isDirectory()) {
				pos = pos.add(offsetPos);
				BufferedImage image = ImageIO.read(file);
				int imageX = image.getWidth(), imageY = image.getHeight();

				for (int x = 0; x < imageX; x++) {
					for (int y = 0; y < imageY; y++) {
						BlockPos position = BlockPos.ZERO;
						int posX = imageX - x, posY = imageY - y;
						switch (axis) {
						case Y:
							position = pos.add(posX, 0, posY);
							break;
						case X:
							position = pos.add(0, posY, posX);
							break;
						case Z:
							position = pos.add(posX, posY, 0);
							break;
						}

						if (world.getBlockState(position).getBlock() instanceof ColorBlock) {
							BlockState oldState = ColorBlock.getOverwrittenState(position, world);
							world.setBlockState(position, oldState);
							if (world.isRemote()) {
								ColorBlockPacketHandler.sendToServer(new ImageColorChangePacket(-2, position,
										oldState));
							}
						}
						continue;
					}
				}
			} else {
				if (file != new File("") && player != null && world.isRemote()) {
					player.sendMessage(
							new TranslationTextComponent("container.image_block.file_non_exists", file.toString()),
							player.getUniqueID());
				}
			}
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.iWorld.applyOrElse((world, pos) -> {
			return !this.isRightBlock(world.getBlockState(pos)) ? false
					: playerIn.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
							(double) pos.getZ() + 0.5D) <= 64.0D;
		}, true);
	}

	public static int getColor(int x, int y, BufferedImage image, PlayerSaves saves) {
		// Getting pixel color by position x and y
		// True bewirkt, dass die Farbe einen Aplha Kanal hat(Transparenz)
		// Alpha = 0 = Transparent, Alpha = 255 = unsdurchsichtig
		Color color = new Color(image.getRGB(x, y), true);
		// Alpha ist die Transparenz. 0 steht für Transparent.
		// Also Wenn der Pixel Transparent ist soll er dies als Weiß darstellen
		if (color.getAlpha() == 0) {
			// Die nutzung der Einstellung ob leere pixel gefüllt werden sollen
			if (saves.fillEmptyFixel()) {
				// Färbt den Pixel auf die dafür eingestellte Farbe
				return saves.getColorToFill();
			} else {
				// Gibt einen negativen wert zurück damit erkannt wird, dass der Pixel nicht
				// gefüllt werden soll
				return -1;
			}
		}
		// Diese Zeile fügt aus Rot Grün und Blau die RGB zahl zusammen:
		// color.getRed() << 16 sorgt dafür, dass Rot an der richtigen stelle steht z.B.
		// statt ff = ff0000
		// "|" sorgt für die übereinanderschreibung der drei werte z.B. aus Rot ff0000,
		// Grün cd00 und Blau 23 wird ffcd23
		int rgb = (color.getRed() << 16 | color.getGreen() << 8 | color.getBlue());
		return rgb;
	}

	public ImageTileEntity getTileEntity() {
		return this.tileEntity;
	}
}