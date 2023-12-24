package com.sigmundgranaas.forgero.fabric.yacl;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.OptionGroup;
import dev.isxander.yacl.api.YetAnotherConfigLib;

import dev.isxander.yacl.gui.controllers.BooleanController;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ForgeroYACLConfigScreenBuilder {
	public static Screen createScreen(Screen parentScreen) {
		return createBuilder().generateScreen(parentScreen);
	}

	private static YetAnotherConfigLib createBuilder() {
		return YetAnotherConfigLib.createBuilder().title(Text.translatable("forgero.config.title")).category(
				ConfigCategory.createBuilder().name(Text.translatable("forgero.config.category.1.title")).tooltip(
						Text.translatable("forgero.config.category.1.title.tooltip")).group(
						OptionGroup.createBuilder().name(Text.translatable("forgero.config.group.1.title")).tooltip(Text.translatable(
								"This text will appear when you hover over the name or focus on the collapse button with Tab.")).option(
								Option.createBuilder(Boolean.class).name(Text.translatable("forgero.config.disableVanillaRecipes")).tooltip(
										Text.translatable("forgero.config.disableVanillaRecipes.tooltip")).binding(
										false, () -> ForgeroConfigurationLoader.configuration.disableVanillaRecipes,
										newValue -> ForgeroConfigurationLoader.configuration.disableVanillaRecipes = newValue
								).controller(BooleanController::new).build()).build()).build()).build();
	}
}
