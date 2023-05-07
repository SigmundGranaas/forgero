package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.client.MinecraftClient;

public class OptionEntryFactory {
	public Optional<ConfigurationEntry> convertToEntry(Field field, Object value, int x, int y) {
		TextWidget text = new TextWidget(0, y + 5, 300, 20, Text.translatable(MessageFormat.format("forgero.menu.options.{0}", field.getName())), MinecraftClient.getInstance().textRenderer);
		ResetButtonWidget reset = new ResetButtonWidget(x + 100, y, 50, 20, ForgeroConfigurationLoader.configuration, field);
		if (value instanceof Boolean) {
			BooleanWidget widget = new BooleanWidget(x + 200, y, 50, 20, ForgeroConfigurationLoader.configuration, field);
			return Optional.of(new DefaultEntry(widget, text, reset));
		} else if (value instanceof Integer) {
			IntInputOption widget = new IntInputOption(MinecraftClient.getInstance().textRenderer, x + 200, y, 50, 20, Text.translatable(MessageFormat.format("forgero.menu.options.{0}", field.getName())), Text.translatable(MessageFormat.format("forgero.menu.options.{0}", field.getName())), ForgeroConfigurationLoader.configuration, field);
			return Optional.of(new DefaultEntry(widget, text, reset));
		}

		return Optional.empty();
	}
}
