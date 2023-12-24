package com.sigmundgranaas.forgero.fabric.yacl;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.ListOption;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.OptionGroup;
import dev.isxander.yacl.api.YetAnotherConfigLib;

import dev.isxander.yacl.gui.controllers.BooleanController;

import dev.isxander.yacl.gui.controllers.string.StringController;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Collections;

public class ForgeroYACLConfigScreenBuilder {
	public static Screen createScreen(Screen parentScreen) {
		return createBuilder().generateScreen(parentScreen);
	}

	private static YetAnotherConfigLib createBuilder() {
		return YetAnotherConfigLib.createBuilder().title(Text.translatable("forgero.config.title"))
				  .category(
							ConfigCategory.createBuilder()
										  .name(Text.translatable("forgero.config.category.main.title"))
										  .tooltip(Text.translatable("forgero.config.category.main.title.tooltip"))
										  .group(ListOption.createBuilder(String.class)
										                    .name(Text.translatable("forgero.config.disabledResources"))
										                    .tooltip(Text.translatable("forgero.config.disabledResources.tooltip"))
										                    .initial("")
										                    .binding(
												                    Collections.emptyList(),
												                    () -> ForgeroConfigurationLoader.configuration.disabledResources,
												                    newValue -> ForgeroConfigurationLoader.configuration.disabledResources = newValue
										                    ).controller(StringController::new).build())
										  .group(ListOption.createBuilder(String.class)
										                    .name(Text.translatable("forgero.config.disabledPacks"))
										                    .tooltip(Text.translatable("forgero.config.disabledPacks.tooltip"))
										                    .initial("")
										                    .binding(
												                    Collections.emptyList(),
												                    () -> ForgeroConfigurationLoader.configuration.disabledPacks,
												                    newValue -> ForgeroConfigurationLoader.configuration.disabledPacks = newValue
										                    ).controller(StringController::new).build())
										  .group(OptionGroup.createBuilder()
															.name(Text.translatable("forgero.config.group.main.title"))
															.tooltip(Text.translatable("forgero.config.group.main.title.tooltip"))
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.disableVanillaRecipes"))
																		 .tooltip(Text.translatable("forgero.config.disableVanillaRecipes.tooltip"))
																		 .binding(
																				false,
																				() -> ForgeroConfigurationLoader.configuration.disableVanillaRecipes,
																				newValue -> ForgeroConfigurationLoader.configuration.disableVanillaRecipes = newValue
																		).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.enableCustomRecipeDeletion"))
																		 .tooltip(Text.translatable("forgero.config.enableCustomRecipeDeletion.tooltip"))
																		 .binding(
																				true,
																				() -> ForgeroConfigurationLoader.configuration.enableCustomRecipeDeletion,
																				newValue -> ForgeroConfigurationLoader.configuration.enableCustomRecipeDeletion = newValue
																		).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.enableRecipesForAllSchematics"))
																		 .tooltip(Text.translatable("forgero.config.enableRecipesForAllSchematics.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.enableRecipesForAllSchematics,
																				 newValue -> ForgeroConfigurationLoader.configuration.enableRecipesForAllSchematics = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.enableUpgradeInCraftingTable"))
																		 .tooltip(Text.translatable("forgero.config.enableUpgradeInCraftingTable.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable,
																				 newValue -> ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.disableVanillaLoot"))
																		 .tooltip(Text.translatable("forgero.config.disableVanillaLoot.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.disableVanillaLoot,
																				 newValue -> ForgeroConfigurationLoader.configuration.disableVanillaLoot = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.disableVanillaLoot"))
																		 .tooltip(Text.translatable("forgero.config.disableVanillaLoot.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.disableVanillaLoot,
																				 newValue -> ForgeroConfigurationLoader.configuration.disableVanillaLoot = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.disableVanillaTools"))
																		 .tooltip(Text.translatable("forgero.config.disableVanillaTools.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.disableVanillaTools,
																				 newValue -> ForgeroConfigurationLoader.configuration.disableVanillaTools = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.convertVanillaRecipesToForgeroTools"))
																		 .tooltip(Text.translatable("forgero.config.convertVanillaRecipesToForgeroTools.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.convertVanillaRecipesToForgeroTools,
																				 newValue -> ForgeroConfigurationLoader.configuration.convertVanillaRecipesToForgeroTools = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.convertVanillaToolLoot"))
																		 .tooltip(Text.translatable("forgero.config.convertVanillaToolLoot.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.convertVanillaToolLoot,
																				 newValue -> ForgeroConfigurationLoader.configuration.convertVanillaToolLoot = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.enableUnbreakableTools"))
																		 .tooltip(Text.translatable("forgero.config.enableUnbreakableTools.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.enableUnbreakableTools,
																				 newValue -> ForgeroConfigurationLoader.configuration.enableUnbreakableTools = newValue
																		 ).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.enableRepairKits"))
																		 .tooltip(Text.translatable("forgero.config.enableRepairKits.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.enableRepairKits,
																				 newValue -> ForgeroConfigurationLoader.configuration.enableRepairKits = newValue
																		 ).controller(BooleanController::new).build())
															.build()).build())
				.category(ConfigCategory.createBuilder()
										.name(Text.translatable("forgero.config.category.debug.title"))
										.tooltip(Text.translatable("forgero.config.category.debug.title.tooltip"))
										.group(OptionGroup.createBuilder()
															.name(Text.translatable("forgero.config.group.debug.title"))
															.tooltip(Text.translatable("forgero.config.group.debug.title.tooltip"))
															.option(Option.createBuilder(Boolean.class)
																			.name(Text.translatable("forgero.config.resourceLogging"))
																			.tooltip(Text.translatable("forgero.config.resourceLogging.tooltip"))
																			.binding(
																				  true,
																				  () -> ForgeroConfigurationLoader.configuration.resourceLogging,
																				  newValue -> ForgeroConfigurationLoader.configuration.resourceLogging = newValue
																			).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		.name(Text.translatable("forgero.config.logDisabledPackages"))
																		.tooltip(Text.translatable("forgero.config.logDisabledPackages.tooltip"))
																		.binding(
																				false,
																				() -> ForgeroConfigurationLoader.configuration.logDisabledPackages,
																				newValue -> ForgeroConfigurationLoader.configuration.logDisabledPackages = newValue
																		).controller(BooleanController::new).build())
															.option(Option.createBuilder(Boolean.class)
																		.name(Text.translatable("forgero.config.exportGeneratedTextures"))
																		.tooltip(Text.translatable("forgero.config.exportGeneratedTextures.tooltip"))
																		.binding(
																				false,
																				() -> ForgeroConfigurationLoader.configuration.exportGeneratedTextures,
																				newValue -> ForgeroConfigurationLoader.configuration.exportGeneratedTextures = newValue
																		).controller(BooleanController::new).build())
															.build()
										)
										.build()
				).build();
	}
}
