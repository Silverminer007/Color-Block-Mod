package com.silverminer.color_block.objects.items;

import java.util.List;

import com.silverminer.color_block.objects.blocks.ColorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ColorToolItem extends Item {

	public ColorToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getPos();
		if (context.getWorld().getBlockState(pos).getBlock() instanceof ColorBlock) {// Pr�ft ob der angelickte Block
																						// der Farbblock ist
			player.swingArm(context.getHand());// Erzeugt die animation vom einsetzten eines Items
			ItemStack item = player.getHeldItem(context.getHand());// Holt das Item mit dem geklickt worden ist
			if (!item.hasDisplayName() || player.isSneaking()) {// Pr�ft ob diese Situation der Part dieser Funtion oder
																// der im ColorBlock ist
				if (!String.valueOf(ColorBlock.getColorStatic(pos)).equals(item.getDisplayName().getString())) {// Pr�ft
																												// ob
																												// der
																												// zu
					// setzende Name
					// ungleich dem
					// jetztigen
					// Namen ist
					item.setDisplayName(new StringTextComponent(String.valueOf(ColorBlock.getColorStatic(pos))));// Setzt
																													// den
																													// Namen
					if (context.getWorld().isRemote()) {// Sorgt daf�r, dass diese Nachricht nur einmal gesedet wird
						player.sendMessage(// Informiert den Spieler auf welche Farbe der Name gestzt wrden ist
								new StringTextComponent("Settet Coloring texture to: "
										+ String.valueOf(ColorBlock.getColorStatic(pos))),
								PlayerEntity.getUUID(player.getGameProfile()));
					}
				}
				return ActionResultType.SUCCESS;
			}
		}

		/**
		 * Macht die �nderungen wirksam
		 */
		World world = context.getWorld();
		BlockState state = world.getBlockState(pos);
		world.setBlockState(pos, state);
		world.notifyBlockUpdate(pos, state, state, 64);
		world.markBlockRangeForRenderUpdate(pos, state, state);

		return super.onItemUse(context);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		String text = "-";
		if (stack.hasDisplayName() && !stack.getDisplayName().getString().replaceAll("\\D", "").isEmpty()) {
			text = stack.getDisplayName().getString();// Sucht nach der Farbe die das Item jetzt setzten w�rde
			text = text.replaceAll("\\D", "");// Entfernt alle nicht nummerischen Zeichen
		}

		if (flagIn.isAdvanced()) {// F�gt den Erweiterten Tooltip zu
			tooltip.add(new TranslationTextComponent("item.color_block.color_tool.tooltip.advanced"));
		} else {// F�gt den einfachen tooltip zu
			tooltip.add(new TranslationTextComponent("item.color_block.color_tool.tooltip.normal"));
		}
		tooltip.add(new StringTextComponent(" '" + text + "'"));// Zeigt die zu setzende Farbe an

		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
}