package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ResetButtonWidget extends ButtonWidget {
	private final Object object;
	private final Field field;

	public ResetButtonWidget(int x, int y, int width, int height, Object object, Field field) {
		super(x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.reset"), button -> {
		});

		this.object = object;
		this.field = field;
	}

	@Override
	public void onPress() {
		super.onPress();

		reset();
	}

	private void reset() {
		try {
			// TODO: Fetch default value for field
			// TODO: Figure out a way to trigger BooleanWidget/ListWidget text update
			this.field.set(this.object, true);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
