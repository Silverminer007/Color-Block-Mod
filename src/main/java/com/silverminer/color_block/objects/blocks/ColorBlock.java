package com.silverminer.color_block.objects.blocks;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.init.InitItems;
import com.silverminer.color_block.init.InitTileEntityTypes;
import com.silverminer.color_block.objects.items.ColorToolItem;
import com.silverminer.color_block.objects.tile_entity.ColorBlockTileEntity;
import com.silverminer.color_block.util.network.ColorBlockPacketHandler;
import com.silverminer.color_block.util.network.SColorChangePacket;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ColorBlock extends Block {

	protected static final Logger LOGGER = LogManager.getLogger(ColorBlock.class);

	public ColorBlock(Properties properties) {
		super(properties);
	}

	public static int getColorStatic(BlockPos pos, World world) {
		return ColorBlock.getColorStatic(world.getTileEntity(pos));
	}

	public static int getColorStatic(TileEntity tEntity) {
		return tEntity instanceof ColorBlockTileEntity ? ((ColorBlockTileEntity) tEntity).getBlockColor() : 0xffffff;
	}

	public static boolean setColorStatic(int rgb, BlockPos pos, World worldIn) {
		TileEntity tEntity = worldIn.getTileEntity(pos);
		return ColorBlock.setColorStatic(rgb, tEntity);
	}

	public static boolean setColorStatic(int rgb, TileEntity tEntity) {
		if (tEntity instanceof ColorBlockTileEntity) {
			tEntity.markDirty();
			((ColorBlockTileEntity) tEntity).setColor(rgb);
			return true;
		}
		return false;
	}

	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {

		ItemStack item = player.getHeldItem(handIn);

		if (player.isSneaking())
			return ActionResultType.PASS;

		/**
		 * Wenn das Item Fabstoff ist setzte die Farbe auf die Farbstofffarbe
		 */
		if (item.getItem() instanceof DyeItem) {
			/**
			 * Holt die Farbe vom Farbstoff und setzt diese
			 */
			ColorBlock.setColorStatic(((DyeItem) item.getItem()).getDyeColor().getTextColor(), pos, worldIn);
		}

		else if (item.getItem() instanceof ColorToolItem) {// Verwendet den Stift teil des Color Tool Items
			/**
			 * Prüft ob diese Situation der Part diese Funtion oder der im Color Tool Item
			 */
			if (item.hasDisplayName() && !player.isSneaking()) {
				try {// Fängt Fehler ab
					/**
					 * Holt die Zahl auf die die Farbe gesetzt werden soll
					 */
					String text = item.getDisplayName().getString();
					int color = Saves.getSystem(player).castStringToInt(text);
					ColorBlock.setColorStatic(color == -1 ? getColorStatic(pos, worldIn) : color, pos, worldIn);
				} catch (NumberFormatException numberformatexception) {
					/**
					 * Warnt den Spieler, dass die Farbe nur auf Zahl werte gesetzt werden kann
					 */
					player.sendMessage(
							new TranslationTextComponent("block.color_block.color_block.error.non_int_value"),
							PlayerEntity.getUUID(player.getGameProfile()));
					return ActionResultType.SUCCESS;
				}
			}
		}

		/**
		 * Sorgt dafür, dass Blöcke an und auf den Farbblock normal gesetzt werden
		 * können
		 */
		else if (item.getItem() instanceof BlockItem) {
			return ActionResultType.PASS;
		}

		else if (item.getItem() == InitItems.MULTI_DYE.get()) {
			if (!worldIn.isRemote()) {
				/**
				 * The int in Random.nextInt(int) is one higher that the max Color to have the
				 * chance of all colors
				 */
				int color = (new Random()).nextInt(16777216);
				ColorBlock.setColorStatic(color, pos, worldIn);
				ColorBlockPacketHandler.sendToAll(new SColorChangePacket(color, pos, player.getEntityId()));
			}
		}

		else {// Öffnet die GUI
			if (!worldIn.isRemote()) {
				TileEntity tile = worldIn.getTileEntity(pos);
				if (tile instanceof ColorBlockTileEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, (ColorBlockTileEntity) tile, pos);
					return ActionResultType.SUCCESS;
				}
			}
		}

		worldIn.setBlockState(pos, state, 64);// Sorgt dafür, dass änderungen übernommen werden
		worldIn.notifyBlockUpdate(pos, state, state, 0);
		worldIn.markBlockRangeForRenderUpdate(pos, state, state);// Sorgt für die veränderungen der Textur

		return ActionResultType.SUCCESS;
	}

	public TileEntity createTileEntity(BlockState state, IBlockReader worldIn) {
		return InitTileEntityTypes.COLOR_BLOCK.get().create();
	}

	public boolean hasTileEntity(BlockState state) {
		return true;
	}
}