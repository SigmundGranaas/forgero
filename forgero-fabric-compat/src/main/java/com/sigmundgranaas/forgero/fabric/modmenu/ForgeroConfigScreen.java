package com.sigmundgranaas.forgero.fabric.modmenu;

import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ForgeroConfigScreen extends Screen {
	protected ForgeroConfigScreen(Text title) {
		super(title);

		ReadConfig();
	}

	public void ReadConfig() {
		TextFieldWidget textFieldWidget = new TextFieldWidget(textRenderer, 0, 0, 700, 700, Text.of("test"));
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
}
