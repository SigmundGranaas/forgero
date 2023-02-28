package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class DefaultEntry extends ConfigurationEntry {
    private final ButtonWidget button;

    private final TextWidget textWidget;

    public DefaultEntry(ButtonWidget button, TextWidget textWidget) {
        this.button = button;
        this.textWidget = textWidget;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(button);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(button);
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        button.y = y;
        button.x = x + 200;
        button.render(matrices, mouseX, mouseY, tickDelta);

        textWidget.y = y;
        textWidget.x = x - 50;
        textWidget.render(matrices, mouseX, mouseY, tickDelta);
    }
}
