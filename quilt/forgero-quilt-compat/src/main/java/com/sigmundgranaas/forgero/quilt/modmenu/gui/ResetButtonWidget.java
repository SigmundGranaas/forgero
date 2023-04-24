package com.sigmundgranaas.forgero.quilt.modmenu.gui;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.quilt.modmenu.ForgeroConfigurationScreen;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class ResetButtonWidget extends ButtonWidget {
	private final Object object;
	private final Field field;

	public ResetButtonWidget(int x, int y, int width, int height, Object object, Field field) {
		super(x - width / 2, y - height / 2, width, height, Text.translatable("forgero.menu.options.reset"), button -> {
		});

		this.object = object;
		this.field = field;
	}

	@Override
	public void onPress() {
		super.onPress();
		reset();
		ForgeroConfigurationLoader.save();
	}

	private void reset() {
		try {
			this.field.set(this.object, ForgeroConfigurationLoader.defaultConfiguration.getByKey(this.field.getName()));
			ForgeroConfigurationScreen.INSTANCE.RebuildConfigScreen();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
	}

}
