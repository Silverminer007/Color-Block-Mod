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
		this.titleX = 60;
		this.ySize = 62;
		this.xSize = 203;
	}

	public void initFields() {
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.nameField = new TextFieldWidget(this.font, i + 62, j + 24, 103, 12,
				new TranslationTextComponent("container.color_block"));
		this.nameField.setCanLoseFocus(false);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(24);
		this.nameField.setResponder(this::updateNameField);
		this.children.add(this.nameField);
		this.setFocusedDefault(this.nameField);
		this.nameField.setFocused2(true);

		NumberingSystem system;
		try {
			system = Saves.getSaves(this.minecraft.player).getSystem();
			system = system == null ? NumberingSystem.DEZ : system;
		} catch (Throwable e) {
			system = NumberingSystem.DEZ;
		}

		this.mode_button = this.addButton(new ColorBlockScreen.ModeButton(this.getGuiLeft() + 173,
				this.getGuiTop() + 18, 20, 20, system.getTextComponent(), (on_Button_Pressed) -> {
					ColorBlockScreen.this.onButtonPressed();
				}, (button, mStack, p_238488_2_, p_238488_3_) -> {
					ColorBlockScreen.this.renderTooltip(mStack,
							new TranslationTextComponent("container.number_system_base"), p_238488_2_, p_238488_3_);
				}, system));

		int color = this.getContainer().getColor();
		String newString = this.mode_button.getZahlenSystem().parseToStringFromDez(color);
		this.nameField.setText(color == -1 ? "" : newString);
		this.addButton(this.nameField);
	}

	private void updateNameField(String nameFieldString) {
		this.setColor(this.mode_button.getZahlenSystem().castStringToInt(nameFieldString),
				this.getContainer().getTileEntity().getPos());
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(ColorBlockScreen.TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
		if (this.mode_button.getZahlenSystem().hasInvaildChar(this.nameField.getText())) {
			this.blit(matrixStack, i + 59, j + 20, 0, this.ySize, 110, 16);// Make the textfield Red
		} else {
			this.blit(matrixStack, i + 59, j + 20, 0, this.ySize + 16, 110, 16);// Use the normal textfield
		}
	}

	public void resize(Minecraft minecraft, int width, int height) {
		String s = this.nameField.getText();
		this.init(minecraft, width, height);
		this.nameField.setText(s);
	}

	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			this.minecraft.player.closeScreen();
		}

		return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.canWrite()
				? super.keyPressed(keyCode, scanCode, modifiers)
				: true;
	}

	protected void init() {
		super.init();
		this.initFields();
	}

	public void onClose() {
		super.onClose();

		this.minecraft.keyboardListener.enableRepeatEvents(false);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
		this.font.func_243248_b(matrixStack, this.title, (float)this.titleX, (float)this.titleY, 4210752);
	}

	public void setColor(int color, BlockPos position) {
		ColorBlock.setColorStatic(color, this.getContainer().getTileEntity());
		ColorBlockPacketHandler.sendToServer(new CColorChangePacket(color, position));
		World world = this.getContainer().getTileEntity().getWorld();
		if (world == null)
			return;
		BlockState state = world.getBlockState(position);
		world.setBlockState(position, state, 64);// Sorgt daf�r, dass �nderungen �bernommen werden
		world.notifyBlockUpdate(position, state, state, 0);
		world.markBlockRangeForRenderUpdate(position, state, state);// Sorgt f�r die ver�nderungen der Textur
	}

	public void onButtonPressed() {
		int castInt = this.mode_button.getZahlenSystem().castStringToInt(this.nameField.getText());
		this.nameField.setText("");

		this.mode_button.setZahlenSystem(this.mode_button.getNextSystem());// Set to the next System

		String newString = this.mode_button.getZahlenSystem().parseToStringFromDez(castInt);

		this.nameField.setText(castInt == -1 ? "" : newString);
		this.nameField.setFocused2(true);

		if (this.minecraft != null) {
			if (this.minecraft.player != null) {
				PlayerSaves saves = Saves.getSaves(this.minecraft.player);
				saves = saves.setSystem(this.mode_button.getZahlenSystem());
				Saves.setOrCreateSaves(this.minecraft.player, saves,
						this.minecraft.player.getEntityWorld().isRemote());
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
			this.setMessage(this.numbering_system.getTextComponent());
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