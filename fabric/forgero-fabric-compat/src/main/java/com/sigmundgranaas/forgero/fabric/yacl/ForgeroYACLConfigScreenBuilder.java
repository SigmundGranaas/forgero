package com.sigmundgranaas.forgero.fabric.yacl;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.ListOption;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.OptionGroup;
import dev.isxander.yacl.api.YetAnotherConfigLib;

import dev.isxander.yacl.gui.controllers.BooleanController;

import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl.gui.controllers.string.StringController;

import dev.isxander.yacl.gui.controllers.string.number.FloatFieldController;
import dev.isxander.yacl.gui.controllers.string.number.IntegerFieldController;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Collections;

public class ForgeroYACLConfigScreenBuilder {
	public static Screen createScreen(Screen parentScreen) {
		return createBuilder().generateScreen(parentScreen);
	}

	private static YetAnotherConfigLib createBuilder() {
		return YetAnotherConfigLib.createBuilder()
				  .title(Text.translatable("forgero.config.title"))
				  .save(ForgeroConfigurationLoader::save)
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
															.name(Text.translatable("forgero.config.group.recipes_upgrades_and_loot.title"))
															.tooltip(Text.translatable("forgero.config.group.recipes_upgrades_and_loot.title.tooltip"))
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
															.build())
										  .group(OptionGroup.createBuilder()
															.name(Text.translatable("forgero.config.group.repairing.title"))
															.tooltip(Text.translatable("forgero.config.group.repairing.title.tooltip"))
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
																				  true,
																				  () -> ForgeroConfigurationLoader.configuration.enableRepairKits,
																				  newValue -> ForgeroConfigurationLoader.configuration.enableRepairKits = newValue
																		  ).controller(BooleanController::new).build()).build())
										  .group(OptionGroup.createBuilder()
														   .name(Text.translatable("forgero.config.group.attributes.title"))
														   .tooltip(Text.translatable("forgero.config.group.attributes.title.tooltip"))
														   .option(Option.createBuilder(Integer.class)
																		 .name(Text.translatable("forgero.config.baseSoulLevelRequirement"))
																		 .tooltip(Text.translatable("forgero.config.baseSoulLevelRequirement.tooltip"))
																		 .binding(
																				 1000,
																				 () -> ForgeroConfigurationLoader.configuration.baseSoulLevelRequirement,
																				 newValue -> ForgeroConfigurationLoader.configuration.baseSoulLevelRequirement = newValue
																		 ).controller(IntegerFieldController::new).build())
														   .option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.useEntityAttributes"))
																		 .tooltip(Text.translatable("forgero.config.useEntityAttributes.tooltip"))
																		 .binding(
																				 true,
																				 () -> ForgeroConfigurationLoader.configuration.useEntityAttributes,
																				 newValue -> ForgeroConfigurationLoader.configuration.useEntityAttributes = newValue
																		 ).controller(BooleanController::new).build())
														   .option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.showAttributeDifference"))
																		 .tooltip(Text.translatable("forgero.config.showAttributeDifference.tooltip"))
																		 .binding(
																				 true,
																				 () -> ForgeroConfigurationLoader.configuration.showAttributeDifference,
																				 newValue -> ForgeroConfigurationLoader.configuration.showAttributeDifference = newValue
																		 ).controller(BooleanController::new).build())
														   .option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.hideRarity"))
																		 .tooltip(Text.translatable("forgero.config.hideRarity.tooltip"))
																		 .binding(
																				 true,
																				 () -> ForgeroConfigurationLoader.configuration.hideRarity,
																				 newValue -> ForgeroConfigurationLoader.configuration.hideRarity = newValue
																		 ).controller(BooleanController::new).build())
														   .option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.weightReducesAttackSpeed"))
																		 .tooltip(Text.translatable("forgero.config.weightReducesAttackSpeed.tooltip"))
																		 .binding(
																				 true,
																				 () -> ForgeroConfigurationLoader.configuration.weightReducesAttackSpeed,
																				 newValue -> ForgeroConfigurationLoader.configuration.weightReducesAttackSpeed = newValue
																		 ).controller(BooleanController::new).build())
														   .option(Option.createBuilder(Float.class)
																		 .name(Text.translatable("forgero.config.minimumAttackSpeed"))
																		 .tooltip(Text.translatable("forgero.config.minimumAttackSpeed.tooltip"))
																		 .binding(
																				 0.5f,
																				 () -> ForgeroConfigurationLoader.configuration.minimumAttackSpeed,
																				 newValue -> ForgeroConfigurationLoader.configuration.minimumAttackSpeed = newValue
																		 ).controller(FloatFieldController::new).build())
														   .option(Option.createBuilder(Boolean.class)
																		 .name(Text.translatable("forgero.config.weightIncreasesHunger"))
																		 .tooltip(Text.translatable("forgero.config.weightIncreasesHunger.tooltip"))
																		 .binding(
																				 false,
																				 () -> ForgeroConfigurationLoader.configuration.weightIncreasesHunger,
																				 newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHunger = newValue
																		 ).controller(BooleanController::new).build())
														   .option(Option.createBuilder(Integer.class)
																		 .name(Text.translatable("forgero.config.weightIncreasesHungerScalar"))
																		 .tooltip(Text.translatable("forgero.config.weightIncreasesHungerScalar.tooltip"))
																		 .binding(
																				 10,
																				 () -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerScalar,
																				 newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerScalar = newValue
																		 ).controller(IntegerFieldController::new).build())
														   .option(Option.createBuilder(Float.class)
																		 .name(Text.translatable("forgero.config.weightIncreasesHungerBaseChance"))
																		 .tooltip(Text.translatable("forgero.config.weightIncreasesHungerBaseChance.tooltip"))
																		 .binding(
																				 0.01f,
																				 () -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerBaseChance,
																				 newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerBaseChance = newValue
																		 ).controller(FloatFieldController::new).build())
														   .option(Option.createBuilder(Integer.class)
																		 .name(Text.translatable("forgero.config.weightIncreasesHungerCenterPoint"))
																		 .tooltip(Text.translatable("forgero.config.weightIncreasesHungerCenterPoint.tooltip"))
																		 .binding(
																				 50,
																				 () -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerCenterPoint,
																				 newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerCenterPoint = newValue
																		 ).controller((var option) -> new IntegerSliderController(option, 0, 200, 1)).build())
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
				)
				.category(ConfigCategory.createBuilder()
										.name(Text.translatable("forgero.config.category.performance.title"))
										.tooltip(Text.translatable("forgero.config.category.performance.title.tooltip"))
										.group(OptionGroup.createBuilder()
															.name(Text.translatable("forgero.config.group.performance.title"))
															.tooltip(Text.translatable("forgero.config.group.performance.title.tooltip"))
															.option(Option.createBuilder(Boolean.class)
																		.name(Text.translatable("forgero.config.buildModelsAsync"))
																		.tooltip(Text.translatable("forgero.config.buildModelsAsync.tooltip"))
																		.binding(
																			  true,
																			  () -> ForgeroConfigurationLoader.configuration.buildModelsAsync,
																			  newValue -> ForgeroConfigurationLoader.configuration.buildModelsAsync = newValue
																		).controller(BooleanController::new).build())
															.build()
										)
										.build()
				).build();
	}
}
