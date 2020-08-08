package com.silverminer.color_block.util.math;

import java.util.ArrayList;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.util.text.StringTextComponent;

public class NumberingSystem {

	public static final ArrayList<NumberingSystem> NUMBERING_SYSTEMS = new ArrayList<NumberingSystem>();

	public static final NumberingSystem DEZ = new NumberingSystem(10, "#D",
			Lists.newArrayList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));

	public static final NumberingSystem HEX = new NumberingSystem(16, "#H",
			Lists.newArrayList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'));

	public static final NumberingSystem BIN = new NumberingSystem(2, "#B", Lists.newArrayList('0', '1'));

	public static final NumberingSystem OCT = new NumberingSystem(8, "#O",
			Lists.newArrayList('0', '1', '2', '3', '4', '5', '6', '7'));

	private final int base;
	private final String shownName;
	private final ArrayList<Character> chars;

	public NumberingSystem(int baseIn, String shownNameIn, ArrayList<Character> allowedCharsIn) {
		this.base = baseIn;
		this.shownName = shownNameIn;
		this.chars = allowedCharsIn;

		NumberingSystem.NUMBERING_SYSTEMS.add(this);
	}

	/**
	 * 
	 * @return An ArrayList of Characters that are used to show the numbers of this
	 *         Numbering System
	 */
	public ArrayList<Character> getAllowedChars() {
		return chars;
	}

	/**
	 * 
	 * @return An String that is show e.g. in the text in the Button of the
	 *         ColorBlockScreen
	 */
	public String getShownName() {
		return shownName;
	}

	/**
	 * 
	 * @return The Base of the Numbering System
	 */
	public int getBase() {
		return base;
	}

	/**
	 * 
	 * @return The ShownName as StringTextComponent
	 */
	public StringTextComponent getTextComponent() {
		return new StringTextComponent(this.getShownName());
	}

	/**
	 * 
	 * @param charIn
	 * @return Wether the given char is in the List of AllowedChars of this
	 *         Numbering System
	 */
	public boolean isAllowedChar(char charIn) {
		return this.getAllowedChars().contains(charIn);
	}

	/**
	 * This Method parse the given String to an Integer. The String is handeled as
	 * the Actuall Numbering System. Only Positive Numbers are allowed
	 * 
	 * @param stringIn
	 * @return The Castet Integer or if an Error occures -1
	 */
	public int castStringToInt(String stringIn) {
		if (!stringIn.isEmpty()) {
			String mTextFieldString = this.removeInvalidChr(stringIn.toLowerCase(Locale.ROOT));
			if(!mTextFieldString.isEmpty()) {
				try {
					return Integer.parseInt(mTextFieldString, this.getBase());
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return -1;
				}
			}
		}
		return -1;
	}

	/**
	 * Parses the given int to an String that shows the int in the Actuall Numbering
	 * System
	 * 
	 * @param intIn
	 * @return The parsed Int as String
	 */
	public String parseToStringFromDez(int intIn) {
		String newString = "";

		if (this.getBase() > this.getAllowedChars().size())
			return newString;

		if (intIn < 0) {
			intIn = -intIn;
		}

		while (intIn != 0) {
			int teilrest = intIn % this.base;
			intIn = intIn / this.base;
			newString = String.valueOf(this.getAllowedChars().get(teilrest)) + newString;
		}

		return newString;
	}

	/**
	 * This Method Removes all Chars from the given String that aren't in the List
	 * of Allowed Chars of this Numbering System
	 * 
	 * @param str
	 * @return the string without invalid chars
	 */
	public String removeInvalidChr(String str) {
		String newString = "";
		for (int i = 0; i < str.length(); i++) {
			if (this.isAllowedChar(str.charAt(i))) {
				newString = newString + String.valueOf(str.charAt(i));
			}
		}
		return newString;
	}

	/**
	 * Detects if in the given String is an invalid char
	 * 
	 * @param str
	 * @return true if in the given String is an invalid char, otherwise false
	 */
	public boolean hasInvaildChar(String str) {
		for (char ch : str.toCharArray()) {
			if (!this.isAllowedChar(ch)) {
				return true;
			}
		}
		return false;
	}

	public int getStringLeghtForInt(int num) {
		return this.parseToStringFromDez(num).length();
	}

	public int getMaxStringLeghtForInt(int num) {
		int highestValue = 0;
		for (NumberingSystem system : NumberingSystem.NUMBERING_SYSTEMS) {
			highestValue = system.parseToStringFromDez(num).length() > highestValue
					? this.parseToStringFromDez(num).length()
					: highestValue;
		}
		return highestValue;
	}

	/**
	 * 
	 * @param baseIn The Base to Search
	 * @return The Numbering System where the base is Similar to the given base. If
	 *         there is no Numbering System with this base it returns
	 *         NumberingSystem.DEZ
	 */
	public static NumberingSystem getByBase(int baseIn) {
		for (NumberingSystem system : NumberingSystem.NUMBERING_SYSTEMS) {
			if (system.getBase() == baseIn) {
				return system;
			}
		}
		return DEZ;
	}
}