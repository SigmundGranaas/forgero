package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import java.util.List;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;

public class DefaultEntry extends ConfigurationEntry {
	private final ClickableWidget button;

	private final ClickableWidget reset;

	private final TextWidget textWidget;

	private final int xDefaultPosition;
	private final int xTextPosition;

	public DefaultEntry(ClickableWidget button, TextWidget textWidget, ClickableWidget reset) {
		this.button = button;
		this.textWidget = textWidget;
		this.reset = reset;
		this.xDefaultPosition = 150;
		this.xTextPosition = -70;
	}

	@Override
	public List<? extends Selectable> selectableChildren() {
		return List.of(button, reset);
	}

	@Override
	public List<? extends Element> children() {
		return List.of(button, reset, textWidget);
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		button.setY(y);
		button.setX(x + xDefaultPosition);
		button.render(matrices, mouseX, mouseY, tickDelta);

		reset.setY(y);
		reset.setX(x + xDefaultPosition + button.getWidth() + 5);
		reset.render(matrices, mouseX, mouseY, tickDelta);

		textWidget.setY(y + 5);
		textWidget.setX(x + xTextPosition);
		textWidget.render(matrices, mouseX, mouseY, tickDelta);
	}
}
