package com.silverminer.color_block.util.saves;

import com.silverminer.color_block.util.math.NumberingSystem;

public class PlayerSaves {

	private NumberingSystem system;

	private int max_image_x;

	private int max_image_y;

	private boolean ignore_image_size;

	private boolean fill_empty_pixel;

	private int color_to_fill;

	public PlayerSaves(NumberingSystem system, int max_image_x, int max_image_y, boolean ignore_image_size, boolean fill_empty_pixel, int color_to_fill) {
		this.system = system;
		this.max_image_x = max_image_x;
		this.max_image_y = max_image_y;
		this.ignore_image_size = ignore_image_size;
		this.fill_empty_pixel = fill_empty_pixel;
		this.color_to_fill = color_to_fill;
	}

	public PlayerSaves() {
		this.system = NumberingSystem.DEZ;
		this.max_image_x = 64;
		this.max_image_y = 64;
		this.ignore_image_size = false;
		this.fill_empty_pixel = true;
		this.color_to_fill = 0xffffff;
	}

	public NumberingSystem getSystem() {
		return this.system;
	}

	public int getMaxImageX() {
		return this.max_image_x;
	}

	public int getMaxImageY() {
		return this.max_image_y;
	}

	public boolean ignoreImageSize() {
		return this.ignore_image_size;
	}

	public boolean fillEmptyFixel() {
		return this.fill_empty_pixel;
	}

	public int getColorToFill() {
		return this.color_to_fill;
	}

	public PlayerSaves setSystem(NumberingSystem system) {
		this.system = system;
		return this;
	}

	public PlayerSaves setMaxImageX(int max_image_x) {
		this.max_image_x = max_image_x;
		return this;
	}

	public PlayerSaves setMaxImageY(int max_image_y) {
		this.max_image_y = max_image_y;
		return this;
	}

	public PlayerSaves setIgnoreImageSize(boolean ignore_image_size) {
		this.ignore_image_size = ignore_image_size;
		return this;
	}

	public PlayerSaves setFillEmtpyPixel(boolean fill_empty_pixel) {
		this.fill_empty_pixel = fill_empty_pixel;
		return this;
	}

	public PlayerSaves setColorToFill(int color_to_fill) {
		this.color_to_fill = color_to_fill;
		return this;
	}
}