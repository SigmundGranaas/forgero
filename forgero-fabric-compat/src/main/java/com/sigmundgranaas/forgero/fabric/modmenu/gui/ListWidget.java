package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ListWidget extends TextFieldWidget {
	protected final String suggestion;

	private final Object object;
	private final Field field;
	private final int maxCharacters = 10;

	public ListWidget(int x, int y, int width, int height, Object object, Field field) {
		super(MinecraftClient.getInstance().textRenderer, x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.error"));

		this.object = object;
		this.field = field;
		this.suggestion = ForgeroConfigurationLoader.defaultConfiguration.getByKey(this.field.getName()).toString().replaceAll("[\\[\\]]", "");

		// Set the max length because the default max length (32) is too short
		setMaxLength(Integer.MAX_VALUE);

		try {
			setText(this.field.get(this.object).toString().replaceAll("[\\[\\]]", ""));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		setSuggestionCustom();
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (!super.charTyped(chr, modifiers)) {
			return false;
		}

		setFieldValue();
		ForgeroConfigurationLoader.save();

		return true;
	}

	@Override
	public void eraseCharacters(int characterOffset) {
		super.eraseCharacters(characterOffset);

		setFieldValue();
		ForgeroConfigurationLoader.save();

		setSuggestionCustom();
	}

	private void setFieldValue() {
		try {
			// TODO: Handle invalid input (maybe add some warning text to the config screen)
			this.field.set(
					this.object,
					Arrays.stream(getText().trim().replaceAll("[\\[\\]]", "").split(",")).toList().stream()
							.map(String::trim)
							.collect(Collectors.toList()));

			setSuggestionCustom();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void setSuggestionCustom() {
		if (getText().length() <= 0) {
			setSuggestion(this.suggestion.substring(0, Math.min(this.maxCharacters - 3, this.suggestion.length())) + (this.suggestion.length() > this.maxCharacters - 3 ? "..." : ""));
		} else {
			setSuggestion("");
		}
	}
}
