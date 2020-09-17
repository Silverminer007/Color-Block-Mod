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
import com.silverminer.color_block.objects.tile_entity.ImageTileEntity;
import com.silverminer.color_block.util.math.NumberingSystem;
import com.silverminer.color_block.util.saves.PlayerSaves;
import com.silverminer.color_block.util.saves.Saves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
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

	public static final ResourceLocation TEXTURE_ADVANCED = new ResourceLocation(ColorBlockMod.MODID,
			"textures/gui/image_screen_advanced.png");

	private boolean isAdvanced = false;

	private TextFieldWidget nameField;

	private TextFieldWidget xOffset, yOffset, zOffset;

	private TextFieldWidget max_image_x, max_image_y, color_to_fill;

	private AxisButton axisButton;

	private RotationButton rotationButton;

	private Button ignore_image_size_button, fill_empty_pixel_button;

	private Button finish_button, advanced_button;

	public boolean has_error = true;

	private final int advancedYSize = 181, normalYSize = 120;

	public ImageScreen(ImageContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, null, titleIn);
		this.field_238742_p_ = 60;
		this.ySize = this.normalYSize;
		this.xSize = 203;
	}

	public void render() {
		this.field_230706_i_.keyboardListener.enableRepeatEvents(true);
		if (Saves.getSaves(this.field_230706_i_.player) == null) {
			Saves.setOrCreateSaves(this.field_230706_i_.player, new PlayerSaves(),
					this.field_230706_i_.player.getEntityWorld().isRemote());
		}
		this.renderNameField();
		this.renderPositionOffset();
		this.renderButtons();
		this.renderAdvancedMode();
	}

	public void renderPositionOffset() {
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		ImageTileEntity tileEntity = this.getContainer().getTileEntity();

		this.xOffset = new TextFieldWidget(this.field_230712_o_, i + 62, j + 50, 28, 12,
				new TranslationTextComponent("container.image_block.xOffset"));
		this.xOffset.setCanLoseFocus(true);
		this.xOffset.setTextColor(-1);
		this.xOffset.setDisabledTextColour(-1);
		this.xOffset.setEnableBackgroundDrawing(false);
		this.xOffset.setMaxStringLength(4);
		this.xOffset.setResponder(this::updateXOffset);
		this.field_230705_e_.add(this.xOffset);

		this.yOffset = new TextFieldWidget(this.field_230712_o_, i + 100, j + 50, 28, 12,
				new TranslationTextComponent("container.image_block.yOffset"));
		this.yOffset.setCanLoseFocus(true);
		this.yOffset.setTextColor(-1);
		this.yOffset.setDisabledTextColour(-1);
		this.yOffset.setEnableBackgroundDrawing(false);
		this.yOffset.setMaxStringLength(4);
		this.yOffset.setResponder(this::updateYOffset);
		this.field_230705_e_.add(this.yOffset);

		this.zOffset = new TextFieldWidget(this.field_230712_o_, i + 138, j + 50, 28, 12,
				new TranslationTextComponent("container.image_block.zOffset"));
		this.zOffset.setCanLoseFocus(true);
		this.zOffset.setTextColor(-1);
		this.zOffset.setDisabledTextColour(-1);
		this.zOffset.setEnableBackgroundDrawing(false);
		this.zOffset.setMaxStringLength(4);
		this.zOffset.setResponder(this::updateZOffset);
		this.field_230705_e_.add(this.zOffset);

		this.xOffset.setText(String.valueOf(tileEntity.getXOffset()));
		this.yOffset.setText(String.valueOf(tileEntity.getYOffset()));
		this.zOffset.setText(String.valueOf(tileEntity.getZOffset()));
	}

	public void renderNameField() {
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		ImageTileEntity tileEntity = this.getContainer().getTileEntity();
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
		String file = tileEntity.getFile().toString();
		if (file.contains("\\") && file.contains(".")) {
			file = file.substring(file.lastIndexOf("\\") + 1, file.lastIndexOf("."));
			this.nameField.setText(file);
		}
	}

	public void renderAdvancedMode() {
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.advancedYSize) / 2;
		PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);

		this.ignore_image_size_button = this.func_230480_a_(new Button(i + 55, j + 114, 20, 20,
				ImageScreen.getBooleanButtonText(saves.ignoreImageSize()), (on_Button_Pressed) -> {
					ImageScreen.this.onMaxImageSizeButtonPressed();
				}, (button, mStack, x, y) -> {
					ImageScreen.this.func_238652_a_(mStack,
							new TranslationTextComponent("container.use_max_image_size"), x, y);
				}));
		this.ignore_image_size_button.field_230694_p_ = false;

		this.fill_empty_pixel_button = this.func_230480_a_(new Button(i + 55, j + 138, 20, 20,
				ImageScreen.getBooleanButtonText(saves.fillEmptyFixel()), (on_Button_Pressed) -> {
					ImageScreen.this.onEmptyPixelButtonPressed();
				}, (button, mStack, x, y) -> {
					ImageScreen.this.func_238652_a_(mStack, new TranslationTextComponent("container.fill_empty_pixel"),
							x, y);
				}));
		this.fill_empty_pixel_button.field_230694_p_ = false;

		this.max_image_x = new TextFieldWidget(this.field_230712_o_, i + 87, j + 120, 28, 12,
				new TranslationTextComponent("container.max_image_x"));
		this.max_image_x.setCanLoseFocus(true);
		this.max_image_x.setTextColor(-1);
		this.max_image_x.setDisabledTextColour(-1);
		this.max_image_x.setEnableBackgroundDrawing(false);
		this.max_image_x.setMaxStringLength(24);
		this.max_image_x.setResponder(this::updateMaxImageX);
		this.field_230705_e_.add(this.max_image_x);
		this.max_image_x.setVisible(false);
		this.max_image_x.setText(String.valueOf(saves.getMaxImageX()));

		this.max_image_y = new TextFieldWidget(this.field_230712_o_, i + 123, j + 120, 28, 12,
				new TranslationTextComponent("container.max_image_y"));
		this.max_image_y.setCanLoseFocus(true);
		this.max_image_y.setTextColor(-1);
		this.max_image_y.setDisabledTextColour(-1);
		this.max_image_y.setEnableBackgroundDrawing(false);
		this.max_image_y.setMaxStringLength(24);
		this.max_image_y.setResponder(this::updateMaxImageY);
		this.field_230705_e_.add(this.max_image_y);
		this.max_image_y.setVisible(false);
		this.max_image_y.setText(String.valueOf(saves.getMaxImageY()));

		this.color_to_fill = new TextFieldWidget(this.field_230712_o_, i + 87, j + 144, 103, 12,
				new TranslationTextComponent("container.color_to_fill"));
		this.color_to_fill.setCanLoseFocus(true);
		this.color_to_fill.setTextColor(-1);
		this.color_to_fill.setDisabledTextColour(-1);
		this.color_to_fill.setEnableBackgroundDrawing(false);
		this.color_to_fill.setMaxStringLength(24);
		this.color_to_fill.setResponder(this::updateColorToFill);
		this.field_230705_e_.add(this.color_to_fill);
		this.color_to_fill.setVisible(false);
		this.color_to_fill.setText(String.valueOf(saves.getColorToFill()));
	}

	public void renderButtons() {
		ImageTileEntity tileEntity = this.getContainer().getTileEntity();
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;

		Rotation rot = tileEntity.getRotation();
		this.rotationButton = this.func_230480_a_(new ImageScreen.RotationButton(i + 173, j + 18, 20, 20,
				ImageScreen.getButtonText(rot), (on_Button_Pressed) -> {
					ImageScreen.this.onRotationButtonPressed();
				}, (button, mStack, x, y) -> {
					ImageScreen.this.func_238652_a_(mStack, new TranslationTextComponent("container.rotationButton"), x,
							y);
				}, rot));

		Axis axis = tileEntity.getAxis();
		this.axisButton = this.func_230480_a_(new ImageScreen.AxisButton(i + 173, j + 42, 20, 20,
				new StringTextComponent(axis.getName2().toUpperCase(Locale.ROOT)), (on_Button_Pressed) -> {
					ImageScreen.this.onAxisButtonPressed();
				}, (button, mStack, x, y) -> {
					ImageScreen.this.func_238652_a_(mStack, new TranslationTextComponent("container.axisButton"), x, y);
				}, axis));

		this.finish_button = this.func_230480_a_(new Button(i + 55, j + 66, 135, 20,
				new TranslationTextComponent("container.finish"), (on_Button_Pressed) -> {
					ImageScreen.this.onFinishButtonPressed();
				}, (button, mStack, x, y) -> {
					ImageScreen.this.func_238652_a_(mStack, new TranslationTextComponent("container.finish"), x, y);
				}));

		this.advanced_button = this.func_230480_a_(new Button(i + 55, j + 90, 135, 20,
				new TranslationTextComponent("container.advanced"), (on_Button_Pressed) -> {
					ImageScreen.this.onAdvancedButtonPressed();
				}, (button, mStack, x, y) -> {
					ImageScreen.this.func_238652_a_(mStack, new TranslationTextComponent("container.advanced"), x, y);
				}));
	}

	private void onRotationButtonPressed() {
		this.rotationButton.setRotation(this.rotationButton.getNextRotation());
		this.getContainer().getTileEntity().setRotation(this.rotationButton.getRotation());
	}

	private void onAxisButtonPressed() {
		this.axisButton.setAxis(this.axisButton.getNextAxis());
		this.getContainer().getTileEntity().setAxis(this.axisButton.getAxis());
	}

	public void onMaxImageSizeButtonPressed() {
		PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);
		Saves.setOrCreateSaves(this.field_230706_i_.player, saves.setIgnoreImageSize(!saves.ignoreImageSize()),
				this.field_230706_i_.player.getEntityWorld().isRemote());
		this.ignore_image_size_button.func_238482_a_(ImageScreen.getBooleanButtonText(saves.ignoreImageSize()));
	}

	public void onEmptyPixelButtonPressed() {
		PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);
		Saves.setOrCreateSaves(this.field_230706_i_.player, saves.setFillEmtpyPixel(!saves.fillEmptyFixel()),
				this.field_230706_i_.player.getEntityWorld().isRemote());
		this.fill_empty_pixel_button.func_238482_a_(ImageScreen.getBooleanButtonText(saves.fillEmptyFixel()));
	}

	public void onFinishButtonPressed() {
		this.getContainer().buildImage(this.field_230706_i_.player);
		this.field_230706_i_.displayGuiScreen((Screen) null);
	}

	public void onAdvancedButtonPressed() {
		this.isAdvanced = !this.isAdvanced;
		if (this.isAdvanced) {
			this.ySize = this.advancedYSize;
			this.ignore_image_size_button.field_230694_p_ = true;
			this.fill_empty_pixel_button.field_230694_p_ = true;
			this.max_image_x.setVisible(true);
			this.max_image_y.setVisible(true);
			this.color_to_fill.setVisible(true);
		} else {
			this.ySize = this.normalYSize;
			this.max_image_x.setVisible(false);
			this.max_image_y.setVisible(false);
			this.color_to_fill.setVisible(false);
			this.ignore_image_size_button.field_230694_p_ = false;
			this.fill_empty_pixel_button.field_230694_p_ = false;
		}
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		this.setPosOfWidget(this.axisButton, i + 173, j + 42);
		this.setPosOfWidget(this.rotationButton, i + 173, j + 18);
		this.setPosOfWidget(this.finish_button, i + 55, j + 66);
		this.setPosOfWidget(this.advanced_button, i + 55, j + 90);
		this.setPosOfWidget(this.nameField, i + 62, j + 24);
		this.setPosOfWidget(this.xOffset, i + 62, j + 50);
		this.setPosOfWidget(this.yOffset, i + 100, j + 50);
		this.setPosOfWidget(this.zOffset, i + 138, j + 50);
	}

	public void setPosOfWidget(Widget widget, int x, int y) {
		widget.field_230690_l_ = x;
		widget.field_230691_m_ = y;
	}

	private void updateNameField(String nameFieldString) {
		File file = !org.apache.commons.lang3.StringUtils.isBlank(nameFieldString)
				? new File(ColorBlockMod.image_path + "\\" + nameFieldString + ".png")
				: new File("");
		PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);

		this.has_error = !file.exists();
		if (file.exists()) {
			try {
				BufferedImage image = ImageIO.read(file);
				this.has_error = !(image.getWidth() <= saves.getMaxImageX()
						&& image.getHeight() <= saves.getMaxImageY());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.getContainer().getTileEntity().setFile(file);
	}

	private void updateColorToFill(String nameFieldString) {
		int color_to_fill = NumberingSystem.DEZ.castStringToInt(this.color_to_fill.getText());
		PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);
		Saves.setOrCreateSaves(this.field_230706_i_.player, saves.setColorToFill(color_to_fill),
				this.field_230706_i_.player.getEntityWorld().isRemote());
	}

	private void updateXOffset(String nameFieldString) {
		String xOffset = this.xOffset.getText();
		int xOffsetP;
		if (xOffset.startsWith("-")) {
			xOffset = xOffset.substring(1, xOffset.length());
			xOffsetP = -NumberingSystem.DEZ.castStringToInt(xOffset);
		} else {
			xOffsetP = NumberingSystem.DEZ.castStringToInt(xOffset);
		}
		this.getContainer().getTileEntity().setXOffset(xOffsetP);
	}

	private void updateYOffset(String nameFieldString) {
		String yOffset = this.yOffset.getText();
		int yOffsetP;
		if (yOffset.startsWith("-")) {
			yOffset = yOffset.substring(1, yOffset.length());
			yOffsetP = -NumberingSystem.DEZ.castStringToInt(yOffset);
		} else {
			yOffsetP = NumberingSystem.DEZ.castStringToInt(yOffset);
		}
		this.getContainer().getTileEntity().setYOffset(yOffsetP);
	}

	private void updateZOffset(String nameFieldString) {
		String zOffset = this.zOffset.getText();
		int zOffsetP;
		if (zOffset.startsWith("-")) {
			zOffset = zOffset.substring(1, zOffset.length());
			zOffsetP = -NumberingSystem.DEZ.castStringToInt(zOffset);
		} else {
			zOffsetP = NumberingSystem.DEZ.castStringToInt(zOffset);
		}
		this.getContainer().getTileEntity().setZOffset(zOffsetP);
	}

	private void updateMaxImageX(String nameFieldString) {
		int max_image_x = NumberingSystem.DEZ.castStringToInt(this.max_image_x.getText());
		PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);
		Saves.setOrCreateSaves(this.field_230706_i_.player, saves.setMaxImageX(max_image_x),
				this.field_230706_i_.player.getEntityWorld().isRemote());
	}

	private void updateMaxImageY(String nameFieldString) {
		int max_image_y = NumberingSystem.DEZ.castStringToInt(this.max_image_y.getText());
		PlayerSaves saves = Saves.getSaves(this.field_230706_i_.player);
		Saves.setOrCreateSaves(this.field_230706_i_.player, saves.setMaxImageY(max_image_y),
				this.field_230706_i_.player.getEntityWorld().isRemote());
	}

	/**
	 * Render Methode für Text Felder und Hintergrund Textur
	 */
	@SuppressWarnings("deprecation")
	public void func_230450_a_(MatrixStack mStack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int i = (this.field_230708_k_ - this.xSize) / 2;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		if (!this.isAdvanced) {
			this.field_230706_i_.getTextureManager().bindTexture(ImageScreen.TEXTURE);
		} else {
			this.field_230706_i_.getTextureManager().bindTexture(ImageScreen.TEXTURE_ADVANCED);
		}
		this.func_238474_b_(mStack, i, j, 0, 0, this.xSize, this.ySize);
		// The File Name TextField
		if (this.has_error && this.nameField.getText() != "") {
			this.func_238474_b_(mStack, i + 59, j + 20, 0, this.ySize, 110, 16);// Make the textfield Red
		} else {
			this.func_238474_b_(mStack, i + 59, j + 20, 0, this.ySize + 16, 110, 16);// Use the normal textfield
		}
		int xPosLine = i + 59;
		int usedTextureX = this.xSize - 32;
		int yPosLine = j + 46;
		// The xOffset Text Field
		if (NumberingSystem.DEZ.hasInvaildChar(this.xOffset.getText(), Lists.newArrayList('-'))) {
			// Make the textfield Red
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize, 32, 16);
		} else {
			// Use The normal TextField
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize + 16, 32, 16);
		}
		xPosLine = i + 98;
		// The yOffset text Field
		if (NumberingSystem.DEZ.hasInvaildChar(this.yOffset.getText(), Lists.newArrayList('-'))) {
			// Make the textfield Red
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize, 32, 16);
		} else {
			// Use The normal TextField
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize + 16, 32, 16);
		}
		xPosLine = i + 137;
		// The zOffset Text Field
		if (NumberingSystem.DEZ.hasInvaildChar(this.zOffset.getText(), Lists.newArrayList('-'))) {
			// Make the textfield Red
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize, 32, 16);
		} else {
			// Use The normal TextField
			this.func_238474_b_(mStack, xPosLine, yPosLine, usedTextureX, this.ySize + 16, 32, 16);
		}

		if (this.isAdvanced) {
			if (NumberingSystem.DEZ.hasInvaildChar(this.max_image_x.getText(), Lists.newArrayList('-'))) {
				// Make the textfield Red
				this.func_238474_b_(mStack, i + 85, j + 116, usedTextureX, this.ySize, 32, 16);
			} else {
				// Use The normal TextField
				this.func_238474_b_(mStack, i + 85, j + 116, usedTextureX, this.ySize + 16, 32, 16);
			}

			if (NumberingSystem.DEZ.hasInvaildChar(this.max_image_y.getText(), Lists.newArrayList('-'))) {
				// Make the textfield Red
				this.func_238474_b_(mStack, i + 121, j + 116, usedTextureX, this.ySize, 32, 16);
			} else {
				// Use The normal TextField
				this.func_238474_b_(mStack, i + 121, j + 116, usedTextureX, this.ySize + 16, 32, 16);
			}

			if (NumberingSystem.DEZ.hasInvaildChar(this.color_to_fill.getText(), Lists.newArrayList('-'))) {
				this.func_238474_b_(mStack, i + 85, j + 140, 0, this.ySize, 110, 16);// Make the textfield Red
			} else {
				this.func_238474_b_(mStack, i + 85, j + 140, 0, this.ySize + 16, 110, 16);// Use the normal textfield
			}
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
		if (this.isAdvanced) {
			this.max_image_x.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
			this.max_image_y.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
			this.color_to_fill.func_230430_a_(p_230452_1_, p_230452_2_, p_230452_3_, p_230452_4_);
		}
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
	protected void func_230451_b_(MatrixStack matrix, int mouseX, int mouseY) {
		if (!this.isAdvanced) {
			this.field_230712_o_.func_243248_b(matrix, this.field_230704_d_, 59, 6, 4210752);
		} else {
			this.field_230712_o_.func_243248_b(matrix, this.field_230704_d_, 59, -24, 4210752);
		}
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

	public static ITextComponent getBooleanButtonText(boolean istrue) {
		ITextComponent text;
		if (istrue) {
			text = new StringTextComponent("+");
		} else {
			text = new StringTextComponent("-");
		}
		return text;
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