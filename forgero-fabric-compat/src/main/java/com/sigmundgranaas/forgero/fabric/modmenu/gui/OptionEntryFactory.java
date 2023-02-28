package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Optional;

public class OptionEntryFactory {
    public Optional<ConfigurationEntry> convertToEntry(Field field, Object value, int x, int y) {
        TextWidget text = new TextWidget(0, y - 5, 300, 20, Text.translatable(MessageFormat.format("forgero.menu.options.{0}", field.getName())), MinecraftClient.getInstance().textRenderer);

        if (value instanceof Boolean) {
            BooleanWidget widget = new BooleanWidget(x + 800, y, 50, 20, ForgeroConfigurationLoader.configuration, field);
            return Optional.of(new DefaultEntry(widget, text));
        }

        return Optional.empty();
    }
}
