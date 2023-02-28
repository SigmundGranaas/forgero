package com.sigmundgranaas.forgero.fabric.modmenu.gui;

import com.sigmundgranaas.forgero.fabric.modmenu.ForgeroConfigurationScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;


public class ConfigurationListWidget extends ElementListWidget<ConfigurationEntry> {

    final ForgeroConfigurationScreen parent;


    public ConfigurationListWidget(ForgeroConfigurationScreen parent, MinecraftClient client, List<ConfigurationEntry> entries) {
        super(client, parent.width + 45, parent.height, 20, parent.height - 32, 20);
        this.parent = parent;
        entries.forEach(this::addEntry);
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 15;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }
}
