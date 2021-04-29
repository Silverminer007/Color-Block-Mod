package com.silverminer.color_block.objects.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.silverminer.color_block.init.InitItems;
import com.silverminer.color_block.objects.items.ColorToolItem;
import com.silverminer.color_block.objects.tile_entity.ColorBlockTileEntity;
import com.silverminer.color_block.util.network.ColorBlockPacketHandler;
import com.silverminer.color_block.util.network.SColorChangePacket;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class SlabColorBlock extends ColorBlock implements IWaterLoggable {
	public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape BOTTOM_SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
	protected static final VoxelShape TOP_SHAPE = Block.makeCuboidShape(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);

	public SlabColorBlock(Properties properties) {
		super(properties);
		this.setDefaultState(
				this.getDefaultState().with(TYPE, SlabType.BOTTOM).with(WATERLOGGED, Boolean.valueOf(false)));
	}

	public boolean isTransparent(BlockState state) {
		return state.get(TYPE) != SlabType.DOUBLE;
	}

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(TYPE, WATERLOGGED);
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
					int color = Saves.getSaves(player).getSystem().castStringToInt(text);
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

		LOGGER.info("Opened State: {}", state);
		worldIn.setBlockState(pos, state, 64);// Sorgt dafür, dass änderungen übernommen werden
		worldIn.notifyBlockUpdate(pos, state, state, 0);
		worldIn.markBlockRangeForRenderUpdate(pos, state, state);// Sorgt für die veränderungen der Textur

		return ActionResultType.SUCCESS;
	}

	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		SlabType slabtype = state.get(TYPE);
		switch (slabtype) {
		case DOUBLE:
			return VoxelShapes.fullCube();
		case TOP:
			return TOP_SHAPE;
		default:
			return BOTTOM_SHAPE;
		}
	}

	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos blockpos = context.getPos();
		BlockState blockstate = context.getWorld().getBlockState(blockpos);
		if (blockstate.isIn(this)) {
			return blockstate.with(TYPE, SlabType.DOUBLE).with(WATERLOGGED, Boolean.valueOf(false));
		} else {
			FluidState fluidstate = context.getWorld().getFluidState(blockpos);
			BlockState blockstate1 = this.getDefaultState().with(TYPE, SlabType.BOTTOM).with(WATERLOGGED,
					Boolean.valueOf(fluidstate.getFluid() == Fluids.WATER));
			Direction direction = context.getFace();
			return direction != Direction.DOWN
					&& (direction == Direction.UP || !(context.getHitVec().y - (double) blockpos.getY() > 0.5D))
							? blockstate1
							: blockstate1.with(TYPE, SlabType.TOP);
		}
	}

	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		ItemStack itemstack = useContext.getItem();
		SlabType slabtype = state.get(TYPE);
		if (slabtype != SlabType.DOUBLE && itemstack.getItem() == this.asItem()) {
			if (useContext.replacingClickedOnBlock()) {
				boolean flag = useContext.getHitVec().y - (double) useContext.getPos().getY() > 0.5D;
				Direction direction = useContext.getFace();
				if (slabtype == SlabType.BOTTOM) {
					return direction == Direction.UP || flag && direction.getAxis().isHorizontal();
				} else {
					return direction == Direction.DOWN || !flag && direction.getAxis().isHorizontal();
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}

	public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
		return state.get(TYPE) != SlabType.DOUBLE ? IWaterLoggable.super.receiveFluid(worldIn, pos, state, fluidStateIn)
				: false;
	}

	public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
		return state.get(TYPE) != SlabType.DOUBLE ? IWaterLoggable.super.canContainFluid(worldIn, pos, state, fluidIn)
				: false;
	}

	/**
	 * Update the provided state given the provided neighbor facing and neighbor
	 * state, returning a new state. For example, fences make their connections to
	 * the passed in state if possible, and wet concrete powder immediately returns
	 * its solidified counterpart. Note that this method should ideally consider
	 * only the specific face passed in.
	 */
	@SuppressWarnings("deprecation")
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.get(WATERLOGGED)) {
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}

		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		switch (type) {
		case LAND:
			return false;
		case WATER:
			return worldIn.getFluidState(pos).isTagged(FluidTags.WATER);
		case AIR:
			return false;
		default:
			return false;
		}
	}
}