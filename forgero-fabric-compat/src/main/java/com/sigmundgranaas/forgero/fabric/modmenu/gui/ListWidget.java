package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import com.sigmundgranaas.forgero.core.Forgero;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ListWidget extends TextFieldWidget {
	protected final Text suggestion;

	private final Object object;
	private final Field field;

	public ListWidget(int x, int y, int width, int height, Object object, Field field) {
		super(MinecraftClient.getInstance().textRenderer, x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.error"));

		// TODO: Fetch suggestion text using default config value for field
		this.suggestion = Text.of("TODO");
		this.object = object;
		this.field = field;

		try {
			setText(this.field.get(this.object).toString());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// Set the max length because the default max length (32) is too short
		setMaxLength(10000);
	}

	@Override
	public void setSuggestion(@Nullable String suggestion) {
		super.setSuggestion(this.suggestion.toString());
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		try {
			this.field.set(this.object, Arrays.stream(getText().split(",")).toList());
			Forgero.LOGGER.info(this.field.get(this.object));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return super.charTyped(chr, modifiers);
	}
}
