package com.sigmundgranaas.forgero.fabric.modmenu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.fabric.modmenu.gui.ConfigurationEntry;
import com.sigmundgranaas.forgero.fabric.modmenu.gui.ConfigurationListWidget;
import com.sigmundgranaas.forgero.fabric.modmenu.gui.OptionEntryFactory;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ForgeroConfigurationScreen extends GameOptionsScreen {
	public static ForgeroConfigurationScreen INSTANCE;

	private ConfigurationListWidget controlsList;

	public ForgeroConfigurationScreen(Screen previous, GameOptions gameOptions) {
		super(previous, gameOptions, Text.translatable("forgero.menu.options"));

		INSTANCE = this;
	}

	@Override
	protected void init() {
		super.init();
		List<ConfigurationEntry> entries = buildConfigScreen();
		this.controlsList = new ConfigurationListWidget(this, this.client, entries);
		this.addSelectableChild(this.controlsList);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Render background texture with vOffset -100 to match Mod Menu's vOffset
		this.renderBackground(context);
		this.controlsList.render(context, mouseX, mouseY, delta);

		//drawCenteredTextWithShadow(context, this.textRenderer, this.title, this.width / 2, 8, 16777215);

		super.render(context, mouseX, mouseY, delta);
	}


	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}

	public List<ConfigurationEntry> buildConfigScreen() {
		var list = new ArrayList<ConfigurationEntry>();
		try {
			int y = this.height / 6;
			int booleanWidgetWithResetHeight = 20;
			int padding = 5;

			var entryFactory = new OptionEntryFactory();
			for (Field field : ForgeroConfigurationLoader.configuration.getClass().getFields()) {
				var value = field.get(ForgeroConfigurationLoader.configuration);
				entryFactory.convertToEntry(field, value, 0, y).ifPresent(list::add);

				y += booleanWidgetWithResetHeight + padding;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		ButtonWidget reload = ButtonWidget.builder(Text.translatable("forgero.menu.options.reload_config"), button -> {
			ForgeroConfigurationLoader.load(FabricLoader.getInstance().getConfigDir());
			RebuildConfigScreen();
		}).width(150).build();
		reload.setX(this.width / 2 - 154);
		reload.setY(this.height - 28);
		this.addDrawableChild(reload);

		ButtonWidget done = ButtonWidget.builder(ScreenTexts.DONE, button -> {
			close();
		}).width(150).build();
		done.setX(this.width / 2 + 4);
		done.setY(this.height - 28);
		this.addDrawableChild(done);
		return list;
	}

	// FIXME: This is a hack that shouldn't be necessary if the widget creation functions are moved into their own class which can track the states
	public void RebuildConfigScreen() {
		clearChildren();
		this.init();
	}
}
