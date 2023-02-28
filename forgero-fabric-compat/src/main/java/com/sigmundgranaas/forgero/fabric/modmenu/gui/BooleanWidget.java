package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class BooleanWidget extends ButtonWidget {
    private final Object object;
    private final Field field;

    public BooleanWidget(int x, int y, int width, int height, Object object, Field field) {
        super(x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.error"), button -> {
        });

        this.object = object;
        this.field = field;
        setMessage(getText());
    }

    @Override
    public void onPress() {
        super.onPress();

        toggle();
        ForgeroConfigurationLoader.save();
    }

    private Text getText() {
        try {
            return (Boolean) this.field.get(this.object) ? Text.translatable("forgero.menu.options.true") : Text.translatable("forgero.menu.options.false");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return Text.translatable("forgero.menu.options.error");
        }
    }

    private void toggle() {
        try {
            this.field.set(this.object, !(Boolean) this.field.get(this.object));
            setMessage(getText());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
