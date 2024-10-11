package com.sigmundgranaas.forgero.content.compat.yacl;

import static com.sigmundgranaas.forgero.core.model.Strategy.PRE_BAKED;

import java.util.Collections;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.model.Strategy;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Forgero config screen builder, uses YACL to generate the config screen.
 * <p>
 * Note: Always check if YACL is loaded using <code>ForgeroCompatInitializer.yacl.get()</code> before invoking any methods inside this class.
 * </p>
 */
public class ForgeroYACLConfigScreenBuilder {
	public static Screen createScreen(Screen parentScreen) {
		return createBuilder().generateScreen(parentScreen);
	}

	private static YetAnotherConfigLib createBuilder() {
		return YetAnotherConfigLib.createBuilder().title(Text.translatable("forgero.config.title")).save(
				ForgeroConfigurationLoader::save).category(MainCategory.buildMainCategory()).category(
				DebugCategory.buildDebugCategory()).category(PerformanceCategory.buildPerformanceCategory()).build();
	}

	private static class MainCategory {
		private static ConfigCategory buildMainCategory() {
			return ConfigCategory.createBuilder().name(Text.translatable("forgero.config.category.main.title")).tooltip(
					Text.translatable("forgero.config.category.main.title.tooltip")).group(
					ListOption.<String>createBuilder().name(Text.translatable("forgero.config.disabledResources")).description(
							OptionDescription.of(Text.translatable("forgero.config.disabledResources.tooltip"))).initial("").binding(
							Collections.emptyList(), () -> ForgeroConfigurationLoader.configuration.disabledResources,
							newValue -> ForgeroConfigurationLoader.configuration.disabledResources = newValue
					).controller(StringControllerBuilder::create).build()).group(
					ListOption.<String>createBuilder().name(Text.translatable("forgero.config.disabledPacks")).description(
							OptionDescription.of(Text.translatable("forgero.config.disabledPacks.tooltip"))).initial("").binding(
							Collections.emptyList(), () -> ForgeroConfigurationLoader.configuration.disabledPacks,
							newValue -> ForgeroConfigurationLoader.configuration.disabledPacks = newValue
					).controller(StringControllerBuilder::create).build()).group(buildRecipesUpgradesAndLootGroup()).group(
					buildRepairingGroup()).group(buildAttributesGroup()).build();
		}

		private static OptionGroup buildRecipesUpgradesAndLootGroup() {
			return OptionGroup.createBuilder().name(Text.translatable("forgero.config.group.recipes_upgrades_and_loot.title")).description(
					OptionDescription.of(Text.translatable("forgero.config.group.recipes_upgrades_and_loot.title.tooltip"))).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.disableVanillaRecipes")).description(
							OptionDescription.of(Text.translatable("forgero.config.disableVanillaRecipes.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.disableVanillaRecipes,
							newValue -> ForgeroConfigurationLoader.configuration.disableVanillaRecipes = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.enableCustomRecipeDeletion")).description(
							OptionDescription.of(Text.translatable("forgero.config.enableCustomRecipeDeletion.tooltip"))).binding(
							true,
							() -> ForgeroConfigurationLoader.configuration.enableCustomRecipeDeletion,
							newValue -> ForgeroConfigurationLoader.configuration.enableCustomRecipeDeletion = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.enableRecipesForAllSchematics")).description(
							OptionDescription.of(Text.translatable("forgero.config.enableRecipesForAllSchematics.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.enableRecipesForAllSchematics,
							newValue -> ForgeroConfigurationLoader.configuration.enableRecipesForAllSchematics = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.enableUpgradeInCraftingTable")).description(
							OptionDescription.of(Text.translatable("forgero.config.enableUpgradeInCraftingTable.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable,
							newValue -> ForgeroConfigurationLoader.configuration.enableUpgradeInCraftingTable = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.disableVanillaLoot")).description(
							OptionDescription.of(Text.translatable("forgero.config.disableVanillaLoot.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.disableVanillaLoot,
							newValue -> ForgeroConfigurationLoader.configuration.disableVanillaLoot = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.disableVanillaTools")).description(
							OptionDescription.of(Text.translatable("forgero.config.disableVanillaTools.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.disableVanillaTools,
							newValue -> ForgeroConfigurationLoader.configuration.disableVanillaTools = newValue
					).controller(BooleanControllerBuilder::create).build()).option(Option.<Boolean>createBuilder().name(
					Text.translatable("forgero.config.convertVanillaRecipesToForgeroTools")).description(
					OptionDescription.of(Text.translatable("forgero.config.convertVanillaRecipesToForgeroTools.tooltip"))).binding(
					false,
					() -> ForgeroConfigurationLoader.configuration.convertVanillaRecipesToForgeroTools,
					newValue -> ForgeroConfigurationLoader.configuration.convertVanillaRecipesToForgeroTools = newValue
			).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.convertVanillaToolLoot")).description(
							OptionDescription.of(Text.translatable("forgero.config.convertVanillaToolLoot.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.convertVanillaToolLoot,
							newValue -> ForgeroConfigurationLoader.configuration.convertVanillaToolLoot = newValue
					).controller(BooleanControllerBuilder::create).build()).build();
		}

		private static OptionGroup buildRepairingGroup() {
			return OptionGroup.createBuilder().name(Text.translatable("forgero.config.group.repairing.title")).description(
					OptionDescription.of(Text.translatable("forgero.config.group.repairing.title.tooltip"))).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.enableUnbreakableTools")).description(
							OptionDescription.of(Text.translatable("forgero.config.enableUnbreakableTools.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.enableUnbreakableTools,
							newValue -> ForgeroConfigurationLoader.configuration.enableUnbreakableTools = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.enableRepairKits")).description(
							OptionDescription.of(Text.translatable("forgero.config.enableRepairKits.tooltip"))).binding(
							true,
							() -> ForgeroConfigurationLoader.configuration.enableRepairKits,
							newValue -> ForgeroConfigurationLoader.configuration.enableRepairKits = newValue
					).controller(BooleanControllerBuilder::create).build()).build();
		}

		private static OptionGroup buildAttributesGroup() {
			return OptionGroup.createBuilder().name(Text.translatable("forgero.config.group.attributes.title")).description(
					OptionDescription.of(Text.translatable("forgero.config.group.attributes.title.tooltip"))).option(
					Option.<Integer>createBuilder().name(Text.translatable("forgero.config.baseSoulLevelRequirement")).description(
							OptionDescription.of(Text.translatable("forgero.config.baseSoulLevelRequirement.tooltip"))).binding(
							1000,
							() -> ForgeroConfigurationLoader.configuration.baseSoulLevelRequirement,
							newValue -> ForgeroConfigurationLoader.configuration.baseSoulLevelRequirement = newValue
					).controller(IntegerFieldControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.useEntityAttributes")).description(
							OptionDescription.of(Text.translatable("forgero.config.useEntityAttributes.tooltip"))).binding(
							true,
							() -> ForgeroConfigurationLoader.configuration.useEntityAttributes,
							newValue -> ForgeroConfigurationLoader.configuration.useEntityAttributes = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.showAttributeDifference")).description(
							OptionDescription.of(Text.translatable("forgero.config.showAttributeDifference.tooltip"))).binding(
							true,
							() -> ForgeroConfigurationLoader.configuration.showAttributeDifference,
							newValue -> ForgeroConfigurationLoader.configuration.showAttributeDifference = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.hideRarity")).description(
							OptionDescription.of(Text.translatable("forgero.config.hideRarity.tooltip"))).binding(
							true,
							() -> ForgeroConfigurationLoader.configuration.hideRarity,
							newValue -> ForgeroConfigurationLoader.configuration.hideRarity = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.weightReducesAttackSpeed")).description(
							OptionDescription.of(Text.translatable("forgero.config.weightReducesAttackSpeed.tooltip"))).binding(
							true,
							() -> ForgeroConfigurationLoader.configuration.weightReducesAttackSpeed,
							newValue -> ForgeroConfigurationLoader.configuration.weightReducesAttackSpeed = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Float>createBuilder().name(Text.translatable("forgero.config.minimumAttackSpeed")).description(
							OptionDescription.of(Text.translatable("forgero.config.minimumAttackSpeed.tooltip"))).binding(
							0.5f,
							() -> ForgeroConfigurationLoader.configuration.minimumAttackSpeed,
							newValue -> ForgeroConfigurationLoader.configuration.minimumAttackSpeed = newValue
					).controller(FloatFieldControllerBuilder::create).build()).option(
					Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.weightIncreasesHunger")).description(
							OptionDescription.of(Text.translatable("forgero.config.weightIncreasesHunger.tooltip"))).binding(
							false,
							() -> ForgeroConfigurationLoader.configuration.weightIncreasesHunger,
							newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHunger = newValue
					).controller(BooleanControllerBuilder::create).build()).option(
					Option.<Integer>createBuilder().name(Text.translatable("forgero.config.weightIncreasesHungerScalar")).description(
							OptionDescription.of(Text.translatable("forgero.config.weightIncreasesHungerScalar.tooltip"))).binding(
							10,
							() -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerScalar,
							newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerScalar = newValue
					).controller(IntegerFieldControllerBuilder::create).build()).option(
					Option.<Float>createBuilder().name(Text.translatable("forgero.config.weightIncreasesHungerBaseChance")).description(
							OptionDescription.of(Text.translatable("forgero.config.weightIncreasesHungerBaseChance.tooltip"))).binding(
							0.01f, () -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerBaseChance,
							newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerBaseChance = newValue
					).controller(FloatFieldControllerBuilder::create).build()).option(
					Option.<Integer>createBuilder().name(Text.translatable("forgero.config.weightIncreasesHungerCenterPoint")).description(
							OptionDescription.of(Text.translatable("forgero.config.weightIncreasesHungerCenterPoint.tooltip"))).binding(
							50,
							() -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerCenterPoint,
							newValue -> ForgeroConfigurationLoader.configuration.weightIncreasesHungerCenterPoint = newValue
					).controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 200).step(1)).build()).build();
		}
	}

	private static class DebugCategory {
		private static ConfigCategory buildDebugCategory() {
			return ConfigCategory.createBuilder().name(Text.translatable("forgero.config.category.debug.title")).tooltip(
					Text.translatable("forgero.config.category.debug.title.tooltip")).group(
					OptionGroup.createBuilder().name(Text.translatable("forgero.config.group.debug.title")).description(
							OptionDescription.of(Text.translatable("forgero.config.group.debug.title.tooltip"))).option(
							Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.resourceLogging")).description(
									OptionDescription.of(Text.translatable("forgero.config.resourceLogging.tooltip"))).binding(
									true,
									() -> ForgeroConfigurationLoader.configuration.resourceLogging,
									newValue -> ForgeroConfigurationLoader.configuration.resourceLogging = newValue
							).controller(BooleanControllerBuilder::create).build()).option(
							Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.logDisabledPackages")).description(
									OptionDescription.of(Text.translatable("forgero.config.logDisabledPackages.tooltip"))).binding(
									false,
									() -> ForgeroConfigurationLoader.configuration.logDisabledPackages,
									newValue -> ForgeroConfigurationLoader.configuration.logDisabledPackages = newValue
							).controller(BooleanControllerBuilder::create).build()).option(
							Option.<Boolean>createBuilder().name(Text.translatable("forgero.config.exportGeneratedTextures")).description(
									OptionDescription.of(Text.translatable("forgero.config.exportGeneratedTextures.tooltip"))).binding(
									false, () -> ForgeroConfigurationLoader.configuration.exportGeneratedTextures,
									newValue -> ForgeroConfigurationLoader.configuration.exportGeneratedTextures = newValue
							).controller(BooleanControllerBuilder::create).build()).build()).build();
		}
	}

	private static class PerformanceCategory {
		private static ConfigCategory buildPerformanceCategory() {
			return ConfigCategory.createBuilder()
					.name(Text.translatable("forgero.config.category.performance.title"))
					.tooltip(
							Text.translatable("forgero.config.category.performance.title.tooltip"))
					.group(
							OptionGroup.createBuilder()
									.name(Text.translatable("forgero.config.group.performance.title"))
									.description(
											OptionDescription.of(Text.translatable("forgero.config.group.performance.title.tooltip")))
									.option(
											Option.<Strategy>createBuilder()
													.name(Text.translatable("forgero.config.modelStrategy"))
													.description(
															OptionDescription.of(Text.translatable("forgero.config.modelStrategy.tooltip")))
													.binding(
															PRE_BAKED,
															() -> ForgeroConfigurationLoader.configuration.modelStrategy,
															newValue -> ForgeroConfigurationLoader.configuration.modelStrategy = newValue
													)
													.controller((option) -> EnumControllerBuilder.create(option).enumClass(Strategy.class))
													.build()
									)
									.build())
					.build();
		}
	}
}
