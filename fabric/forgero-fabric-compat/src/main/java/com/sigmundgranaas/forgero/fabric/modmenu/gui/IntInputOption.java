package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import java.lang.reflect.Field;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class IntInputOption extends TextFieldWidget {
	private final Object object;
	private final Field field;

	public IntInputOption(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message, Object object, Field field) {
		super(textRenderer, x, y, width, height, null, message);
		this.object = object;
		this.field = field;
		try {
			this.setText(String.valueOf(field.get(this.object)));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void updateField(String text) {
		try {
			Integer value = Integer.parseInt(text);
			field.set(object, value);
		} catch (NumberFormatException | IllegalAccessException e) {
			Forgero.LOGGER.warn("Unable to set {} as integer value", text);
			Forgero.LOGGER.warn(e);
		}
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (Character.isDigit(chr)) {
			boolean result = super.charTyped(chr, modifiers);
			updateField(getText());
			return result;
		} else {
			return false;
		}
	}
}
