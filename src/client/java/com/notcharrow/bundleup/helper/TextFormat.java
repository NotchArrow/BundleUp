package com.notcharrow.bundleup.helper;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextFormat {

	public static Text styledText(String message) {
		MutableText styledText = Text.literal(message);

		Style textStyle = Style.EMPTY
			.withColor(Formatting.YELLOW);
		styledText.setStyle(textStyle);

		MutableText prefixText = Text.literal("[Bundle Up] ");

		Style prefixStyle = Style.EMPTY
			.withColor(Formatting.GOLD);
		prefixText.setStyle(prefixStyle);

		styledText = prefixText.append(styledText);

		return styledText;
	}
}
