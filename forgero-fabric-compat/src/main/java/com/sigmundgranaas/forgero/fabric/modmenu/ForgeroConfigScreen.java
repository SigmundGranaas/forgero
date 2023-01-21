package com.sigmundgranaas.forgero.fabric.modmenu;

import com.sigmundgranaas.forgero.fabric.option.OptionConvertible;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class ForgeroConfigScreen extends GameOptionsScreen {
	private Screen previous;
	private ButtonListWidget list;

	public ForgeroConfigScreen(Screen previous) {
		super(previous, MinecraftClient.getInstance().options, Text.translatable("forgero.menu.options"));
		this.previous = previous;
	}

	@Override
	protected void init() {
		super.init();

		BuildConfigScreen();
	}

	public void BuildConfigScreen() {
		this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
		this.list.addAll(configurationAsOptions());
		this.addSelectableChild(this.list);
		this.addDrawableChild(
				new ButtonWidget(10, 10, 10, 10, ScreenTexts.DONE, (button) -> {
					// TODO: Save config
//					ModMenuConfigManager.save();
					this.client.setScreen(this.previous);
				})
		);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		// Render background texture with vOffset -100 to match Mod Menu's vOffset
		this.renderBackgroundTexture(-100);

		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);

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

	public SimpleOption<?>[] configurationAsOptions() {
		ArrayList<SimpleOption<?>> options = new ArrayList<>();
		for (Field field : ForgeroConfigurationLoader.configuration.getClass().getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && field.getType().equals(Boolean.class)) {
				try {
					options.add(((OptionConvertible) field.get(ForgeroConfigurationLoader.configuration)).asOption());
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return options.toArray(SimpleOption[]::new);
	}
}
