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
import com.silverminer.color_block.util.Config;

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
	}

	public void buildImage(@Nullable PlayerEntity playerIn) {
		try {
			File file = this.tileEntity.getFile();
			file = file == null ? new File("") : file;
			if (file.exists() && !file.isDirectory()) {
				BlockPos pos = this.tileEntity.getPos().add(this.tileEntity.getOffsetPos());
				World world = this.tileEntity.getWorld();
				LOGGER.info("World is Remote: {}", world.isRemote());
				LOGGER.info("Is this.player.getEntityWorld() Remote: {}", this.player.getEntityWorld().isRemote());
				LOGGER.info("Is Player World Remote: {}",
						playerIn == null ? null : playerIn.getEntityWorld().isRemote());
				BufferedImage image = ImageIO.read(file);
				int imageX = image.getWidth(), imageY = image.getHeight();

				Rotation rot = this.tileEntity.getRotation();
				Axis axis = this.tileEntity.getAxis();

				if ((imageX <= Config.IMAGE_MAX_X && imageY <= Config.IMAGE_MAX_Y) || Config.IGNORE_IMAGE_SIZE) {
					for (int x = 0; x < imageX; x++) {
						for (int y = 0; y < imageY; y++) {
							BlockPos position = BlockPos.ZERO;
							int posX = 0, posY = 0;
							switch (rot) {
							case NONE:
								posX = x;
								posY = imageY - y;
								break;
							case CLOCKWISE_90:
								posX = imageY - y;
								posY = imageX - x;
								break;
							case CLOCKWISE_180:
								posX = imageX - x;
								posY = y;
								break;
							case COUNTERCLOCKWISE_90:
								posX = y;
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

							int color = getColor(x, y, image);
							if (!(color < 0)) {
								BlockState state = InitBlocks.COLOR_BLOCK.get().getDefaultState();
								world.setBlockState(position, state);
								if (state.getBlock().hasTileEntity(state)) {
									world.addTileEntity(state.getBlock().createTileEntity(state, world));
									ColorBlock.setColorStatic(color, position, world);
								}
								state = world.getBlockState(position);
								world.markBlockRangeForRenderUpdate(position, state, state);
								world.notifyBlockUpdate(position, state, state, 64);
							} else {
								world.setBlockState(position, Blocks.AIR.getDefaultState());
							}
							continue;
						}
					}
				} else {
					if (world.isRemote()) {
						this.player.sendMessage(new TranslationTextComponent("container.image_block.to_big_image"),
								this.player.getUniqueID());
					}
				}
			} else {
				if (file != new File("")) {
					if (this.tileEntity.getWorld().isRemote()) {
						this.player.sendMessage(new TranslationTextComponent("container.image_block.file_non_exists"),
								this.player.getUniqueID());
					}
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

	public static int getColor(int x, int y, BufferedImage image) {
		// Getting pixel color by position x and y
		// True bewirkt, dass die Farbe einen Aplha Kanal hat(Transparenz)
		// Alpha = 0 = Transparent, Alpha = 255 = unsdurchsichtig
		Color color = new Color(image.getRGB(x, y), true);
		// Alpha ist die Transparenz. 0 steht für Transparent.
		// Also Wenn der Pixel Transparent ist soll er dies als Weiß darstellen
		if (color.getAlpha() == 0) {
			// Die nutzung der Einstellung ob leere pixel gefüllt werden sollen
			if (Config.FILL_EMPTY_PIXEL) {
				// Färbt den Pixel auf die dafür eingestellte Farbe
				return Config.COLOR_TO_FILL;
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