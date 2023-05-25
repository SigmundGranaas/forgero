package com.sigmundgranaas.forgero.fabric.initialization;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;
import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.fabric.ForgeroInitializer;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.fabric.initialization.datareloader.DataPipeLineReloader;
import com.sigmundgranaas.forgero.fabric.initialization.datareloader.DisassemblyReloader;
import com.sigmundgranaas.forgero.fabric.initialization.datareloader.LootConditionReloadListener;
import com.sigmundgranaas.forgero.fabric.initialization.registrar.CommandRegistrar;
import com.sigmundgranaas.forgero.fabric.initialization.registrar.DynamicItemsRegistrar;
import com.sigmundgranaas.forgero.fabric.initialization.registrar.StateItemRegistrar;
import com.sigmundgranaas.forgero.fabric.initialization.registrar.TreasureLootRegistrar;
import com.sigmundgranaas.forgero.fabric.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.AllPartToAllSchematicsGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.MaterialPartTagGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.PartToSchematicGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.PartTypeTagGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.RepairKitResourceGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.SchematicPartTagGenerator;
import com.sigmundgranaas.forgero.minecraft.common.registry.registrar.AttributesRegistrar;
import com.sigmundgranaas.forgero.minecraft.common.registry.registrar.LootFunctionRegistrar;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import net.minecraft.resource.ResourceType;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

/**
 * The ForgeroPostInitialization class handles the post-initialization phase of the Forgero mod.
 * This phase involves registering various game elements such as blocks, items, commands, and data reload listeners.
 * <p>
 * Post-initialization is typically used for actions that must occur after all states have been loaded into Forgero,
 * such as cross-mod integrations or actions that need complete game data like registering recipes.
 * <p>
 * This class implements the ForgeroInitializedEntryPoint interface, which defines the contract for
 * the Forgero mod's post-initialization phase, which supplies a StateService object that can be used to interact with Forgero's States.
 */
public class ForgeroPostInit implements ForgeroInitializedEntryPoint {
	public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

	/**
	 * The onInitialized method is called after all mods have been loaded.
	 * This method initiates the registration of various game elements.
	 *
	 * @param stateService The state service provides services related to game states.
	 */
	@Override
	public void onInitialized(StateService stateService) {
		registerBlocks();
		registerItems(stateService);
		registerTreasureLoot();
		registerCommands();
		registerLootFunctions();
		registerItemAttributes();
		registerDisassemblyReloadListener();
		registerDataReloadListener();
		registerLootConditionReloadListener();
		registerRecipeSerializers();
		registerAARPRecipes(stateService);
	}

	private void registerBlocks() {
		Registry.register(Registry.BLOCK, ASSEMBLY_STATION, ASSEMBLY_STATION_BLOCK);
		Registry.register(Registry.ITEM, ASSEMBLY_STATION, ASSEMBLY_STATION_ITEM);
		Registry.register(Registry.SCREEN_HANDLER, ASSEMBLY_STATION, ASSEMBLY_STATION_SCREEN_HANDLER);
	}

	/**
	 * The registerItems method registers the items defined in the mod.
	 * It uses the StateItemRegistrar class to handle the registration.
	 * <p>
	 * Dynamic items are those whose are loaded based on different settings or configurations
	 *
	 * @param stateService The state service provides services related to Forgero states.
	 */
	private void registerItems(StateService stateService) {
		new StateItemRegistrar(stateService).registerItem(Registry.ITEM);
		new DynamicItemsRegistrar().register();
	}

	/**
	 * The registerTreasureLoot method registers treasure loot injection for the mod.
	 */
	private void registerTreasureLoot() {
		TreasureLootRegistrar.getInstance().register();
	}

	/**
	 * The registerCommands method registers the commands used by the mod.
	 * Commands provide ways for players to create some default Forgero structures to get started with the mod.
	 */
	private void registerCommands() {
		new CommandRegistrar().register();
	}

	/**
	 * The registerLootFunctions method registers the loot functions used by the mod.
	 * The Loot functions alter the state of looted Forgero items to give them special conditions or levels
	 */
	private void registerLootFunctions() {
		new LootFunctionRegistrar().register();
	}

	/**
	 * The registerItemAttributes method registers the entity attributes used in the mod.
	 */
	private void registerItemAttributes() {
		new AttributesRegistrar().register();
	}

	/**
	 * The registerDisassemblyReloadListener method registers a reload listener for disassembly station recipes.
	 */
	private void registerDisassemblyReloadListener() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DisassemblyReloader());
	}

	/**
	 * The registerDataReloadListener method registers a reload listener for data the data pipeline to reload the Foregero pack configuration to update states.
	 */
	private void registerDataReloadListener() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DataPipeLineReloader());
	}

	/**
	 * The registerLootConditionReloadListener method registers a reload listener for Conditions that can be applied to items
	 */
	private void registerLootConditionReloadListener() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new LootConditionReloadListener());
	}

	/**
	 * The registerRecipeSerializers registers recipe serializers for the mod.
	 * Recipe serializers are needed for all the different recipes used to craft Forgero tools and parts
	 */
	private void registerRecipeSerializers() {
		RecipeRegistry.INSTANCE.registerRecipeSerializers();
	}


	/**
	 * The registerAarpRecipes method registers AARP recipes for the mod.
	 * These recipes are handled by AARP as a dynamic resource pack
	 *
	 * @param service The state service provides services related to game states.
	 */
	private void registerAARPRecipes(StateService service) {
		ARRPGenerator.register(new RepairKitResourceGenerator(ForgeroConfigurationLoader.configuration, service));
		if (ForgeroConfigurationLoader.configuration.enableRecipesForAllSchematics) {
			ARRPGenerator.register(() -> new AllPartToAllSchematicsGenerator(service, new PartToSchematicGenerator.SchematicRecipeCreator(), new PartToSchematicGenerator.AllVariantFilter()));
		} else {
			ARRPGenerator.register(() -> new PartToSchematicGenerator(service, new PartToSchematicGenerator.SchematicRecipeCreator(), new PartToSchematicGenerator.BaseVariantFilter()));
		}

		ARRPGenerator.register(() -> new MaterialPartTagGenerator(service));
		ARRPGenerator.register(() -> new SchematicPartTagGenerator(service));
		ARRPGenerator.register(() -> new PartTypeTagGenerator(service));
		ARRPGenerator.generate(service);
	}
}
