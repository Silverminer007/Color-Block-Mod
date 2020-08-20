package com.silverminer.color_block.gui.container;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitBlocks;
import com.silverminer.color_block.init.InitContainerType;
import com.silverminer.color_block.objects.blocks.ColorBlock;
import com.silverminer.color_block.util.Config;
import com.silverminer.color_block.util.saves.ColorBlockSaveHelper;
import com.silverminer.color_block.util.saves.ImageTransferPacket;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
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

	public ImageContainer(int windowId, PlayerInventory playerInventory, IWorldPosCallable iWorld) {
		super(InitContainerType.IMAGE_CONTAINER.get(), windowId);
		this.iWorld = iWorld;
		this.player = playerInventory.player;
	}

	public ImageContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data) {
		this(windowId, playerInv, IWorldPosCallable.DUMMY);
	}

	public boolean isRightBlock(BlockState state) {
		return state.isIn(InitBlocks.IMAGE_BLOCK.get());
	}

	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		try {
			ImageTransferPacket packet = Saves.getImageOrDefault(playerIn, new ImageTransferPacket());
			File file = packet.getFile();
			file = file == null ? new File("") : file;
			if (file.exists() && !file.isDirectory()) {
				BlockPos pos = Saves.getPosition(playerIn).add(packet.getOffsetPos());
				World playerWorld = playerIn.getEntityWorld();
				BufferedImage image = ImageIO.read(file);
				int imageX = image.getWidth(), imageY = image.getHeight();

				Rotation rot = packet.getRotation();
				Axis axis = packet.getAxis();

				if ((imageX <= Config.IMAGE_MAX_X && imageY <= Config.IMAGE_MAX_Y) || Config.IGNORE_IMAGE_SIZE) {
					for (int x = 0; x < imageX; x++) {
						for (int y = 0; y < imageY; y++) {
							BlockPos position = new BlockPos(-1, -1, -1);
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
								ColorBlock.addColor(new ColorBlockSaveHelper(position, color,
										playerWorld.func_230315_m_().toString()));

								playerWorld.setBlockState(position, InitBlocks.COLOR_BLOCK.get().getDefaultState());
								BlockState state = playerWorld.getBlockState(position);
								playerWorld.markBlockRangeForRenderUpdate(position, state, state);
								playerWorld.notifyBlockUpdate(position, state, state, 64);
							} else {
								playerWorld.setBlockState(position, Blocks.AIR.getDefaultState());
							}
							continue;
						}
					}
				} else {
					if (playerWorld.isRemote()) {
						playerIn.sendMessage(new TranslationTextComponent("container.image_block.to_big_image"),
								playerIn.getUniqueID());
					}
				}
			} else {
				if (file != new File("")) {
					if (playerIn.getEntityWorld().isRemote()) {
						playerIn.sendMessage(new TranslationTextComponent("container.image_block.file_non_exists"),
								playerIn.getUniqueID());
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
			if (Config.FILL_EMPTY_PIXEL) {
				return Config.COLOR_TO_FILL;
			} else {
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

	public PlayerEntity getPlayer() {
		return this.player;
	}
}