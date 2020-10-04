package com.silverminer.color_block.gui.screen;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.gui.container.ColorBlockContainer;
import com.silverminer.color_block.objects.blocks.ColorBlock;
import com.silverminer.color_block.util.math.NumberingSystem;
import com.silverminer.color_block.util.network.CColorChangePacket;
import com.silverminer.color_block.util.network.ColorBlockPacketHandler;
import com.silverminer.color_block.util.saves.PlayerSaves;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColorBlockScreen extends ContainerScreen<ColorBlockContainer> {

	protected static final Logger LOGGER = LogManager.getLogger(ColorBlockScreen.class);

	public static final ResourceLocation TEXTURE = new ResourceLocation(ColorBlockMod.MODID,
			"textures/gui/color_block_screen_background.png");

	private TextFieldWidget nameField;

	public ModeButton mode_button;

	public boolean hasInvalidChar = false;

	public ColorBlockScreen(ColorBlockContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, null, titleIn);
		this.field_238742_p_ = 60;
		this.ySize = 62;
		this.xSize = 203;
	}

	public void func_230453_j_() {
		this.field_230706_i_.keyboardListener.enableRepeatEvents(true);
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		this.nameField = new TextFieldWidget(this.field_230712_o_, i + 62, j + 24, 103, 12,
				new TranslationTextComponent("container.color_block"));
		this.nameField.setCanLoseFocus(false);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(24);
		this.nameField.setResponder(this::updateNameField);
		this.field_230705_e_.add(this.nameField);
		this.setFocusedDefault(this.nameField);
		this.nameField.setFocused2(true);

		NumberingSystem system;
		try {
			system = Saves.getSaves(this.field_230706_i_.player).getSystem();
			system = system == null ? NumberingSystem.DEZ : system;
		} catch (Throwable e) {
			system = NumberingSystem.DEZ;
		}

		this.mode_button = this.func_230480_a_(new ColorBlockScreen.ModeButton(this.getGuiLeft() + 173,
				this.getGuiTop() + 18, 20, 20, system.getTextComponent(), (on_Button_Pressed) -> {
					ColorBlockScreen.this.onButtonPressed();
				}, (button, mStack, p_238488_2_, p_238488_3_) -> {
					ColorBlockScreen.this.func_238652_a_(mStack,
							new TranslationTextComponent("container.number_system_base"), p_238488_2_, p_238488_3_);
				}, system));

		int color = this.getContainer().getColor();
		String newString = this.mode_button.getZahlenSystem().parseToStringFromDez(color);
		this.nameField.setText(color == -1 ? "" : newString);
		this.func_231035_a_(this.nameField);
	}

	private void updateNameField(String nameFieldString) {
		this.setColor(this.mode_button.getZahlenSystem().castStringToInt(nameFieldString),
				this.getContainer().getTileEntity().getPos());
	}

	@SuppressWarnings("deprecation")
	public void func_230450_a_(MatrixStack mStack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.field_230706_i_.getTextureManager().bindTexture(ColorBlockScreen.TEXTURE);
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		this.func_238474_b_(mStack, i, j, 0, 0, this.xSize, this.ySize);
		if (this.mode_button.getZahlenSystem().hasInvaildChar(this.nameField.getText())) {
			this.func_238474_b_(mStack, i + 59, j + 20, 0, this.ySize, 110, 16);// Make the textfield Red
		} else {
			this.func_238474_b_(mStack, i + 59, j + 20, 0, this.ySize + 16, 110, 16);// Use the normal textfield
		}
	}

	public void func_231152_a_(Minecraft minecraft, int p_231152_2_, int p_231152_3_) {
		String s = this.nameField.getText();
		this.func_231158_b_(minecraft, p_231152_2_, p_231152_3_);
		this.nameField.setText(s);
	}

	public void func_230430_a_(MatrixStack p_230452_1_, int p_230452_2_, int p_230452_3_, float p_230452_4_) {
		super.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
		this.nameField.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
	}

	public boolean func_231046_a_(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
		if (p_231046_1_ == 256) {
			this.field_230706_i_.player.closeScreen();
		}

		return !this.nameField.func_231046_a_(p_231046_1_, p_231046_2_, p_231046_3_) && !this.nameField.canWrite()
				? super.func_231046_a_(p_231046_1_, p_231046_2_, p_231046_3_)
				: true;
	}

	protected void func_231160_c_() {
		super.func_231160_c_();
		this.func_230453_j_();
	}

	public void func_231164_f_() {
		super.func_231164_f_();

		this.field_230706_i_.keyboardListener.enableRepeatEvents(false);
	}

	@Override
	protected void func_230451_b_(MatrixStack matrix, int p_230451_2_, int p_230451_3_) {
		this.field_230712_o_.func_243248_b(matrix, this.field_230704_d_, (float) this.field_238742_p_,
				(float) this.field_238743_q_, 4210752);
	}

	public void setColor(int color, BlockPos position) {
		ColorBlock.setColorStatic(color, this.getContainer().getTileEntity());
		ColorBlockPacketHandler.sendToServer(new CColorChangePacket(color, position));
		World world = this.getContainer().getTileEntity().getWorld();
		if (world == null)
			return;
		BlockState state = world.getBlockState(position);
		world.setBlockState(position, state, 64);// Sorgt dafür, dass änderungen übernommen werden
		world.notifyBlockUpdate(position, state, state, 0);
		world.markBlockRangeForRenderUpdate(position, state, state);// Sorgt für die veränderungen der Textur
	}

	public void onButtonPressed() {
		int castInt = this.mode_button.getZahlenSystem().castStringToInt(this.nameField.getText());
		this.nameField.setText("");

		this.mode_button.setZahlenSystem(this.mode_button.getNextSystem());// Set to the next System

		String newString = this.mode_button.getZahlenSystem().parseToStringFromDez(castInt);

		this.nameField.setText(castInt == -1 ? "" : newString);
		this.nameField.setFocused2(true);

		if (this.field_230706_i_ != null) {
			if (this.field_230706_i_.player != null) {
				PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);
				saves = saves.setSystem(this.mode_button.getZahlenSystem());
				Saves.setOrCreateSaves(this.field_230706_i_.player, saves,
						this.field_230706_i_.player.getEntityWorld().isRemote());
			}
		}
	}

	public class ModeButton extends Button {

		private NumberingSystem numbering_system;

		public ModeButton(int xPosition, int yPosition, int xSize, int ySize, ITextComponent text, IPressable onPressed,
				ITooltip tooltip, NumberingSystem system) {
			super(xPosition, yPosition, xSize, ySize, text, onPressed, tooltip);
			this.numbering_system = system;
		}

		public NumberingSystem getZahlenSystem() {
			return this.numbering_system;
		}

		public void setZahlenSystem(NumberingSystem systemIn) {
			this.numbering_system = systemIn;
			this.func_238482_a_(this.numbering_system.getTextComponent());
		}

		public NumberingSystem getNextSystem() {
			ArrayList<NumberingSystem> values = NumberingSystem.NUMBERING_SYSTEMS;
			int position = values.indexOf(this.getZahlenSystem());
			if (!(position < values.size() - 1)) {
				return values.get(0);
			} else {
				return values.get(position + 1);
			}
		}
	}
}