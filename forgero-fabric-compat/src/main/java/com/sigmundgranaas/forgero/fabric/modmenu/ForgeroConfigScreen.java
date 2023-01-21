package com.sigmundgranaas.forgero.fabric.modmenu;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.fabric.modmenu.gui.BooleanWidget;
import com.sigmundgranaas.forgero.fabric.modmenu.gui.ResetButtonWidget;
import com.sigmundgranaas.forgero.fabric.modmenu.gui.TextWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

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
		createBooleanWidgetWithReset(this.width / 2 - 105, this.height / 2, Text.translatable("forgero.menu.options.test"), button -> {
			ForgeroConfigurationLoader.configuration.resourceLogging = !ForgeroConfigurationLoader.configuration.resourceLogging;
		}, button -> {
			ForgeroConfigurationLoader.configuration.resourceLogging = true;
		});
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

	public void createBooleanWidgetWithReset(int x, int y, Text optionName, ButtonWidget.PressAction toggleAction, ButtonWidget.PressAction resetAction) {
		this.addDrawableChild(new TextWidget(x, y - 5, 300, 20, optionName, button -> {
		}, MinecraftClient.getInstance().textRenderer));
		this.addDrawableChild(new BooleanWidget(x + 160, y, toggleAction));
		this.addDrawableChild(new ResetButtonWidget(x + 215, y, resetAction));
	}
}
