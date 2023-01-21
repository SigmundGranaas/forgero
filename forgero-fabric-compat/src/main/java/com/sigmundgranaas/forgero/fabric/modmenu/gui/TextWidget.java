package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

public class TextWidget extends PressableTextWidget {
	public TextWidget(int x, int y, int width, int height, Text text, PressAction onPress, TextRenderer textRenderer) {
		super(x, y, width, height, text, onPress, textRenderer);
	}

	@Override
	public boolean isHovered() {
		return false;
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}
}
