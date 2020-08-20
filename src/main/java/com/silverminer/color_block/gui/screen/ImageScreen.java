package com.silverminer.color_block.gui.screen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.silverminer.color_block.ColorBlockMod;
import com.silverminer.color_block.gui.container.ImageContainer;
import com.silverminer.color_block.util.Config;
import com.silverminer.color_block.util.math.NumberingSystem;
import com.silverminer.color_block.util.saves.ImageTransferPacket;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageScreen extends ContainerScreen<ImageContainer> {

	protected static final Logger LOGGER = LogManager.getLogger(ImageScreen.class);

	public static final ResourceLocation TEXTURE = new ResourceLocation(ColorBlockMod.MODID,
			"textures/gui/image_screen.png");

	private TextFieldWidget nameField;

	private TextFieldWidget xOffset, yOffset, zOffset;

	private AxisButton axisButton;

	private RotationButton rotationButton;

	public static File image_path = new File(Minecraft.getInstance().gameDir.getAbsoluteFile() + "\\Images");

	public boolean has_error = true;

	public ImageScreen(ImageContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, null, titleIn);
		this.field_238742_p_ = 60;
		this.ySize = 93;
		this.xSize = 203;
	}

	public void render() {
		this.field_230706_i_.keyboardListener.enableRepeatEvents(true);
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		this.nameField = new TextFieldWidget(this.field_230712_o_, i + 62, j + 24, 103, 12,
				new TranslationTextComponent("container.image_block"));
		this.nameField.setCanLoseFocus(true);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(24);
		this.nameField.setResponder(this::updateNameField);
		this.field_230705_e_.add(this.nameField);
		this.setFocusedDefault(this.nameField);

		this.xOffset = new TextFieldWidget(this.field_230712_o_, i + 62, j + 50, 28, 12,
				new TranslationTextComponent("container.image_block.xOffset"));
		this.xOffset.setCanLoseFocus(true);
		this.xOffset.setTextColor(-1);
		this.xOffset.setDisabledTextColour(-1);
		this.xOffset.setEnableBackgroundDrawing(false);
		this.xOffset.setMaxStringLength(2);
		this.xOffset.setResponder(this::updateXOffset);
		this.field_230705_e_.add(this.xOffset);

		this.yOffset = new TextFieldWidget(this.field_230712_o_, i + 100, j + 50, 28, 12,
				new TranslationTextComponent("container.image_block.yOffset"));
		this.yOffset.setCanLoseFocus(true);
		this.yOffset.setTextColor(-1);
		this.yOffset.setDisabledTextColour(-1);
		this.yOffset.setEnableBackgroundDrawing(false);
		this.yOffset.setMaxStringLength(2);
		this.yOffset.setResponder(this::updateYOffset);
		this.field_230705_e_.add(this.yOffset);

		this.zOffset = new TextFieldWidget(this.field_230712_o_, i + 139, j + 50, 28, 12,
				new TranslationTextComponent("container.image_block.zOffset"));
		this.zOffset.setCanLoseFocus(true);
		this.zOffset.setTextColor(-1);
		this.zOffset.setDisabledTextColour(-1);
		this.zOffset.setEnableBackgroundDrawing(false);
		this.zOffset.setMaxStringLength(2);
		this.zOffset.setResponder(this::updateZOffset);
		this.field_230705_e_.add(this.zOffset);

		this.rotationButton = this.func_230480_a_(new ImageScreen.RotationButton(this.getGuiLeft() + 173,
				this.getGuiTop() + 18, 20, 20, ImageScreen.getButtonText(Rotation.NONE), (on_Button_Pressed) -> {
					ImageScreen.this.onRotationButtonPressed();
				}, (button, mStack, p_238488_2_, p_238488_3_) -> {
					ImageScreen.this.func_238652_a_(mStack, new TranslationTextComponent("container.rotationButton"),
							p_238488_2_, p_238488_3_);
				}, Rotation.NONE));

		this.axisButton = this.func_230480_a_(new ImageScreen.AxisButton(this.getGuiLeft() + 173, this.getGuiTop() + 44,
				20, 20, new StringTextComponent(Axis.Y.getName2().toUpperCase(Locale.ROOT)), (on_Button_Pressed) -> {
					ImageScreen.this.onAxisButtonPressed();
				}, (button, mStack, p_238488_2_, p_238488_3_) -> {
					ImageScreen.this.func_238652_a_(mStack, new TranslationTextComponent("container.axisButton"),
							p_238488_2_, p_238488_3_);
				}, Axis.Y));

		ImageTransferPacket packet = new ImageTransferPacket();
		Saves.setOrCreateImage(this.getContainer().getPlayer().getUniqueID(), packet);

		this.xOffset.setText(String.valueOf(packet.getXOffset()));
		this.yOffset.setText(String.valueOf(packet.getYOffset()));
		this.zOffset.setText(String.valueOf(packet.getZOffset()));
	}

	private void onRotationButtonPressed() {
		this.rotationButton.setRotation(this.rotationButton.getNextRotation());
		ImageTransferPacket packet = Saves.getImageOrDefault(this.getContainer().getPlayer(), new ImageTransferPacket())
				.setRotation(this.rotationButton.getRotation());
		Saves.setOrCreateImage(this.getContainer().getPlayer().getUniqueID(), packet);
	}

	private void onAxisButtonPressed() {
		this.axisButton.setAxis(this.axisButton.getNextAxis());
		ImageTransferPacket packet = Saves.getImageOrDefault(this.getContainer().getPlayer(), new ImageTransferPacket())
				.setAxis(this.axisButton.getAxis());
		Saves.setOrCreateImage(this.getContainer().getPlayer().getUniqueID(), packet);
	}

	private void updateNameField(String nameFieldString) {
		File file = !org.apache.commons.lang3.StringUtils.isBlank(nameFieldString) ? new File(image_path + "\\" + nameFieldString + ".png")
				: new File("");

		this.has_error = !file.exists();
		if (file.exists()) {
			try {
				BufferedImage image = ImageIO.read(file);
				this.has_error = !(image.getWidth() <= Config.IMAGE_MAX_X && image.getHeight() <= Config.IMAGE_MAX_Y);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ImageTransferPacket packet = Saves.getImageOrDefault(this.getContainer().getPlayer(), new ImageTransferPacket())
				.setFile(file);
		Saves.setOrCreateImage(this.getContainer().getPlayer().getUniqueID(), packet);
	}

	private void updateXOffset(String nameFieldString) {
		nameFieldString = NumberingSystem.DEZ.removeInvalidChr(nameFieldString);
		ImageTransferPacket packet = Saves.getImageOrDefault(this.getContainer().getPlayer(), new ImageTransferPacket())
				.setXOffset(NumberingSystem.DEZ.castStringToInt(nameFieldString));
		Saves.setOrCreateImage(this.getContainer().getPlayer().getUniqueID(), packet);
	}

	private void updateYOffset(String nameFieldString) {
		nameFieldString = NumberingSystem.DEZ.removeInvalidChr(nameFieldString);
		ImageTransferPacket packet = Saves.getImageOrDefault(this.getContainer().getPlayer(), new ImageTransferPacket())
				.setYOffset(NumberingSystem.DEZ.castStringToInt(nameFieldString));
		Saves.setOrCreateImage(this.getContainer().getPlayer().getUniqueID(), packet);
	}

	private void updateZOffset(String nameFieldString) {
		nameFieldString = NumberingSystem.DEZ.removeInvalidChr(nameFieldString);
		ImageTransferPacket packet = Saves.getImageOrDefault(this.getContainer().getPlayer(), new ImageTransferPacket())
				.setZOffset(NumberingSystem.DEZ.castStringToInt(nameFieldString));
		Saves.setOrCreateImage(this.getContainer().getPlayer().getUniqueID(), packet);
	}

	@SuppressWarnings("deprecation")
	public void func_230450_a_(MatrixStack mStack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.field_230706_i_.getTextureManager().bindTexture(ImageScreen.TEXTURE);
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		this.func_238474_b_(mStack, i, j, 0, 0, this.xSize, this.ySize);
		// The File Name TextField
		if (this.has_error && this.nameField.getText() != "") {
			this.func_238474_b_(mStack, i + 59, j + 20, 0, this.ySize, 110, 16);// Make the textfield Red
		} else {
			this.func_238474_b_(mStack, i + 59, j + 20, 0, this.ySize + 16, 110, 16);// Use the normal textfield
		}
		int xPosLine = i + 59;
		int usedTextureX = 171;
		int yPosLine = j + 46;
		// The xOffset Text Field
		if (NumberingSystem.DEZ.hasInvaildChar(this.xOffset.getText())) {
			// Make the textfield Red
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize, 32, 16);
		} else {
			// Use The normal TextField
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize + 16, 32, 16);
		}
		xPosLine = i + 98;
		// The yOffset text Field
		if (NumberingSystem.DEZ.hasInvaildChar(this.yOffset.getText())) {
			// Make the textfield Red
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize, 32, 16);
		} else {
			// Use The normal TextField
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize + 16, 32, 16);
		}
		xPosLine = i + 137;
		// The zOffset Text Field
		if (NumberingSystem.DEZ.hasInvaildChar(this.zOffset.getText())) {
			// Make the textfield Red
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize, 32, 16);
		} else {
			// Use The normal TextField
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize + 16, 32, 16);
		}
	}

	public void func_231152_a_(Minecraft minecraft, int p_231152_2_, int p_231152_3_) {
		String s = this.nameField.getText();
		this.func_231158_b_(minecraft, p_231152_2_, p_231152_3_);
		this.nameField.setText(s);
	}

	/**
	 * This Method is useful to render the text in the TextFields
	 */
	public void func_230430_a_(MatrixStack p_230452_1_, int p_230452_2_, int p_230452_3_, float p_230452_4_) {
		super.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
		this.nameField.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
		this.xOffset.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
		this.yOffset.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
		this.zOffset.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
	}

	public boolean func_231046_a_(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
		if (p_231046_1_ == 256) {
			this.field_230706_i_.player.closeScreen();
		}

		return !this.nameField.func_231046_a_(p_231046_1_, p_231046_2_, p_231046_3_) && !this.nameField.canWrite()
				? super.func_231046_a_(p_231046_1_, p_231046_2_, p_231046_3_)
				: true;
	}

	/**
	 * This Method is called when the Screen is opened
	 */
	protected void func_231160_c_() {
		super.func_231160_c_();
		this.render();
	}

	/**
	 * This Method is called when the Screen is closed
	 */
	public void func_231164_f_() {
		super.func_231164_f_();

		this.field_230706_i_.keyboardListener.enableRepeatEvents(false);
	}

	@Override
	protected void func_230451_b_(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
		this.field_230712_o_.func_238422_b_(p_230451_1_, this.field_230704_d_, (float) this.field_238742_p_,
				(float) this.field_238743_q_, 4210752);
	}

	public static ITextComponent getButtonText(Rotation rot) {
		String name = "";
		switch (rot) {
		case NONE:
			name = "0";
			break;
		case CLOCKWISE_90:
			name = "90";
			break;
		case CLOCKWISE_180:
			name = "180";
			break;
		case COUNTERCLOCKWISE_90:
			name = "270";
			break;
		}
		return new StringTextComponent(name);
	}

	public class AxisButton extends Button {

		private Axis axis;

		public AxisButton(int xPosition, int yPosition, int xSize, int ySize, ITextComponent text, IPressable onPressed,
				ITooltip tooltip, Axis axis) {
			super(xPosition, yPosition, xSize, ySize, text, onPressed, tooltip);
			this.axis = axis;
		}

		public Axis getAxis() {
			return this.axis;
		}

		public void setAxis(Axis axisIn) {
			this.axis = axisIn;
			this.func_238482_a_(new StringTextComponent(this.getAxis().getName2().toUpperCase(Locale.ROOT)));
		}

		public Axis getNextAxis() {
			ArrayList<Axis> values = Lists.newArrayList(Axis.values());
			int position = values.indexOf(this.axis);
			if (!(position < values.size() - 1)) {
				return values.get(0);
			} else {
				return values.get(position + 1);
			}
		}
	}

	public class RotationButton extends Button {

		private Rotation rotation;

		public RotationButton(int xPosition, int yPosition, int xSize, int ySize, ITextComponent text,
				IPressable onPressed, ITooltip tooltip, Rotation rotation) {
			super(xPosition, yPosition, xSize, ySize, text, onPressed, tooltip);
			this.rotation = rotation;
		}

		public Rotation getRotation() {
			return this.rotation;
		}

		public void setRotation(Rotation rotationIn) {
			this.rotation = rotationIn;

			this.func_238482_a_(ImageScreen.getButtonText(this.getRotation()));
		}

		public Rotation getNextRotation() {
			return this.getRotation().add(Rotation.CLOCKWISE_90);
		}
	}
}