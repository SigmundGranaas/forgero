package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class BooleanWidget extends ButtonWidget {
	public static final int width = 50;
	// Don't make the button higher than 20 units because funny things will happen
	public static final int height = 20;

	public BooleanWidget(int x, int y, PressAction onPress) {
		super(x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.loading"), onPress);

		setMessage(getText());
	}

	public Text getText() {
		var trueTextContent = Text.translatable("forgero.menu.options.true").getContent();

		if (getMessage().getContent().equals(trueTextContent)) {
			return Text.translatable("forgero.menu.options.false");
		} else {
			return Text.translatable("forgero.menu.options.true");
		}
	}

	@Override
	public void onPress() {
		super.onPress();

		setMessage(getText());
	}
}
