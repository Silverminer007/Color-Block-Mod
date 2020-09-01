package com.silverminer.color_block.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.silverminer.color_block.gui.screen.ImageScreen;

public class Config {

	public static Integer IMAGE_MAX_X = 64;

	public static Integer IMAGE_MAX_Y = 64;

	public static Boolean IGNORE_IMAGE_SIZE = false;

	public static Boolean FILL_EMPTY_PIXEL = true;

	public static Integer COLOR_TO_FILL = 0xffffff;

	protected static final Logger LOGGER = LogManager.getLogger(Config.class);

	public static void readImageConfig() {
		try {
			String s = "Max_Image_X=64 #The Max Wigth of an Image that is read in by Minecraft/Color-Block-Mod. High Values takes a long while(In some case over hours)\r\n"
					+ "Max_Image_Y=64 #The Max Hight of an Image that is read in by Minecraft/Color-Block-Mod. High Values takes a long while(In some case over hours)\r\n"
					+ "Ignore_Image_Size=false #Take Off the two settings below. The Game will ignore the Size and simply build the Image\r\n"
					+ "Fill_empty_pixel=true #If true empty pixel will be placed with white Block. Other way the pixel will be replaced with air\r\n"
					+ "Color_to_fill=16777215 #If line below is true, this takes with which color the empty pixel are filled(In rgb-Dezimal - Default: White(16777215))";
			File file = new File(ImageScreen.image_path + "\\Config");
			if (!file.exists()) {
				file.mkdir();
			}
			file = new File(file + "\\config.txt");
			if (!file.exists()) {
				PrintWriter out = new PrintWriter(file);
				out.println(s);
				out.close();
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;

			while ((line = reader.readLine()) != null) {
				line = line.replace(" ", "");
				if (line.startsWith("Max_Image_X=")) {
					line = line.substring(line.indexOf("=") + 1, line.indexOf("#"));
					Config.IMAGE_MAX_X = Integer.valueOf(line);
				} else if (line.startsWith("Max_Image_Y=")) {
					line = line.substring(line.indexOf("=") + 1, line.indexOf("#"));
					Config.IMAGE_MAX_Y = Integer.valueOf(line);
				} else if (line.startsWith("Ignore_Image_Size=")) {
					line = line.substring(line.indexOf("=") + 1, line.indexOf("#"));
					Config.IGNORE_IMAGE_SIZE = Boolean.valueOf(line);
				} else if (line.startsWith("Fill_empty_pixel=")) {
					line = line.substring(line.indexOf("=") + 1, line.indexOf("#"));
					Config.FILL_EMPTY_PIXEL = Boolean.valueOf(line);
				} else if (line.startsWith("Color_to_fill=")) {
					line = line.substring(line.indexOf("=") + 1, line.indexOf("#"));
					Config.COLOR_TO_FILL = Integer.valueOf(line);
				}
			}

			reader.close();

		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
			LOGGER.error("Color-Block-Mod Config read-in errored: Using default config");
		}
	}
}