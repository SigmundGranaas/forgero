package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ResetButtonWidget extends ButtonWidget {
	public static final int width = 50;
	// Don't make the button higher than 20 units because funny things will happen
	public static final int height = 20;

	public ResetButtonWidget(int x, int y, PressAction onPress) {
		super(x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.reset"), onPress);
	}
}
