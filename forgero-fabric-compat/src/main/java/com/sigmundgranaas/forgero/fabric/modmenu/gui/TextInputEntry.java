package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class TextInputEntry extends ConfigurationEntry {
    @Override
    public List<? extends Selectable> selectableChildren() {
        return null;
    }

    @Override
    public List<? extends Element> children() {
        return null;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

    }
}
