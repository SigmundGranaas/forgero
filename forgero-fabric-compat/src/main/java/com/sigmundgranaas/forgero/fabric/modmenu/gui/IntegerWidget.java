package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class IntegerWidget extends TextFieldWidget {
    private final Object object;
    private final Field field;

    public IntegerWidget(int x, int y, int width, int height, Object object, Field field) {
        super(MinecraftClient.getInstance().textRenderer, x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.error"));
        this.object = object;
        this.field = field;
        // Set the max length because the default max length (32) is too short
        setMaxLength(Integer.MAX_VALUE);
        try {
            setText(this.field.get(this.object).toString().replaceAll("[\\[\\]]", ""));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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

    }

    private void setFieldValue() {
        try {
            String value = getText();
            int number = Integer.parseInt(value);
            this.field.set(
                    this.object,
                    number > 0 ? number : 1);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
