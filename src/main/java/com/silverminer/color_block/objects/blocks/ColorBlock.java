package com.silverminer.color_block.objects.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.gui.container.ColorBlockContainer;
import com.silverminer.color_block.init.InitItems;
import com.silverminer.color_block.objects.items.ColorToolItem;
import com.silverminer.color_block.util.saves.Saves;
import com.silverminer.color_block.util.saves.ColorBlockSaveHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ColorBlock extends Block {

	private static final Logger LOGGER = LogManager.getLogger(ColorBlock.class);

	public ColorBlock(Properties properties) {
		super(properties);
	}

	public static int getColorStatic(BlockPos pos) {
		for (int i = 0; i < ColorBlockMod.COLOR_BLOCKS.size(); i++) {
			ColorBlockSaveHelper helper = ColorBlockMod.COLOR_BLOCKS.get(i);
			if (helper != null) {
				if (helper.getPosition().equals(pos)) {// Prüft ob die Position mit der gesuchten übereinstimmt
					return helper.getColor();// gibt die registierte Farbe zurück
				}
			}
		}
		return 0xffffff;// Standart Farbe wenn der Block nicht in der Liste ist oder neu gesetzt wurde
	}

	/**
	 * Called by ItemBlocks after a block is set in the world, to allow post-place
	 * logic
	 */
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack stack) {
		if (!worldIn.isRemote()) {// Sorgt dafür, dass der Block nur 1 mal in die liste kommt
			// Erstellt eine neue Speicher einheit
			ColorBlockSaveHelper helper = new ColorBlockSaveHelper(pos, getColorStatic(pos),
					worldIn.func_230315_m_().toString());
			ColorBlock.addColor(helper);
		}
	}

	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onReplaced(state, worldIn, pos, newState, isMoving);
		if (!ColorBlock.removeColor(pos)) {
			ColorBlockMod.LOGGER.error(
					"An Error happened in ColorBlockMod: The Game tried to remove an non placed or saved Block. Please report this error!");
		}
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
			this.setColor(((DyeItem) item.getItem()).getDyeColor().getTextColor(), pos);
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
					this.setColor(color == -1 ? getColorStatic(pos) : color, pos);// Setzt die Farbe
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
			if (worldIn.isRemote()) {
				/**
				 * The int in Random.nextInt(int) is one higher that the max Color to have the
				 * chance of all colors
				 */
				this.setColor((new Random()).nextInt(16777216), pos);
			}
		}

		else {// Öffnet die GUI
			Saves.setOrCreatePos(player, pos);
			if (!worldIn.isRemote()) {
				player.openContainer(state.getContainer(worldIn, pos));
			}
		}

		worldIn.setBlockState(pos, state, 64);// Sorgt dafür, dass änderungen übernommen werden
		worldIn.notifyBlockUpdate(pos, state, state, 0);
		worldIn.markBlockRangeForRenderUpdate(pos, state, state);// Sorgt für die veränderungen der Textur

		return ActionResultType.SUCCESS;
	}

	public ColorBlock setColor(int rgb, BlockPos pos) {
		ColorBlock.setColorStatic(rgb, pos);
		return this;
	}

	public static void setColorStatic(int rgb, BlockPos pos) {
		for (int i = 0; i < ColorBlockMod.COLOR_BLOCKS.size(); i++) {
			ColorBlockSaveHelper helper = ColorBlockMod.COLOR_BLOCKS.get(i);
			if (helper.getPosition().equals(pos)) {
				helper.setColor(rgb);// Setzt die Farbe
				if (ColorBlockMod.colorBlocksSavedData != null) {
					ColorBlockMod.colorBlocksSavedData.markDirty();// Makiert die änderung der Liste
				} else {
					LOGGER.error("ColorBlock Saved Data wasn't inizialized", ColorBlockMod.colorBlocksSavedData);
				}
				LOGGER.info("Settet the Color of ColorBlock At Position: {}" + " to Color: {}", pos, rgb);
				return;
			}
		}
	}

	public static boolean removeColor(BlockPos pos) {
		for (int i = 0; i < ColorBlockMod.COLOR_BLOCKS.size(); i++) {// Durchsucht die Liste nach dem gerade
			// zerstörten Block um ihn aus der Liste zu
			// entfernen
			ColorBlockSaveHelper helper = ColorBlockMod.COLOR_BLOCKS.get(i);
			if (helper.getPosition().equals(pos)) {
				ColorBlockMod.COLOR_BLOCKS.remove(helper);
				if (ColorBlockMod.colorBlocksSavedData != null) {
					ColorBlockMod.colorBlocksSavedData.markDirty();// Makiert, dass die Liste verändert wurde
				} else {
					LOGGER.error("ColorBlock Saved Data wasn't inizialized", ColorBlockMod.colorBlocksSavedData);
				}
				return true;// Sorgt dafür, dass nur eine Speichereinheit entfent wird
			}
		}
		return false;
	}

	public static boolean addColor(ColorBlockSaveHelper helper) {
		// Prüft ob die Speichereinheit schon in der Liste ist
		for (int i = 0; i < ColorBlockMod.COLOR_BLOCKS.size(); i++) {
			ColorBlockSaveHelper save = ColorBlockMod.COLOR_BLOCKS.get(i);
			if (save
					.getPosition()
					.equals(helper
							.getPosition())) {
				if (save.getColor() != helper.getColor()) {
					ColorBlockMod.COLOR_BLOCKS.remove(i);
					break;
				} else {
					return false;
				}
			}
		}
		ColorBlockMod.COLOR_BLOCKS.add(helper);// Fügt anderen falls die einheit der Liste zu
		if (ColorBlockMod.colorBlocksSavedData != null) {
			ColorBlockMod.colorBlocksSavedData.markDirty();// Makiert, dass die Liste sich geändert hat
			return true;
		} else {
			LOGGER.error("ColorBlock Saved Data wasn't inizialized: {}", ColorBlockMod.colorBlocksSavedData);
			return false;
		}
	}

	@Nullable
	public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
		return new SimpleNamedContainerProvider((windowId, playerInventory, player) -> {
			return new ColorBlockContainer(windowId, playerInventory, IWorldPosCallable.of(worldIn, pos));
		}, new TranslationTextComponent("container.color_block"));
	}
}