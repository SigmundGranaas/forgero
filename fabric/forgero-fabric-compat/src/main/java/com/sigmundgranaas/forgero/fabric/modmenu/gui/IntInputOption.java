package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class IntInputOption extends EditBoxWidget {
	private final Object object;
	private final Field field;

	public IntInputOption(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message, Object object, Field field) {
		super(textRenderer, x, y, width, height, placeholder, message);
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
