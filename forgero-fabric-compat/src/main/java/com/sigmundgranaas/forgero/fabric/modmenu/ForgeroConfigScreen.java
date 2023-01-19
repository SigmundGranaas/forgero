package com.sigmundgranaas.forgero.fabric.modmenu;

import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ForgeroConfigScreen extends GameOptionsScreen {
	private Screen previous;
	public ForgeroConfigScreen(Screen previous) {
		super(previous, MinecraftClient.getInstance().options, Text.translatable("forgero.menu.options"));
		this.previous = previous;
	}

	@Override
	protected void init() {
		super.init();
		ReadConfig();
	}

	public void ReadConfig() {
		TextFieldWidget textFieldWidget = new TextFieldWidget(textRenderer, (this.width / 2) - 200, this.height / 2, 200, 32, Text.literal("test"));
		this.addDrawableChild(textFieldWidget);

		for (Field field : ForgeroSettings.SETTINGS.getClass().getFields()) {
//				if (field.get(ForgeroSettings.SETTINGS) == null) {
//					continue;
//				}

			if (field.getType() == boolean.class) {
				TextFieldWidget textFieldWidget2 = new TextFieldWidget(textRenderer, 0, 0, 50, 100, Text.of("test"));
				this.addDrawableChild(textFieldWidget2);
			}
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		// Render background texture with vOffset -100 to match Mod Menu's vOffset
		this.renderBackgroundTexture(-100);

		for (Element element : children()) {
			if (element instanceof Drawable drawable) {
				drawable.render(matrices, mouseX, mouseY, delta);
			}
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}
}
