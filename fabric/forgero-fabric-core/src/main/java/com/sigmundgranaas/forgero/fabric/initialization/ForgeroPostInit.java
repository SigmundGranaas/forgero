package com.sigmundgranaas.forgero.fabric.initialization;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;
import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.LootCondition;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ConditionData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.LootEntryData;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.ForgeroInitializer;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.fabric.command.CommandRegistry;
import com.sigmundgranaas.forgero.fabric.item.StateToItemConverter;
import com.sigmundgranaas.forgero.fabric.loot.TreasureInjector;
import com.sigmundgranaas.forgero.fabric.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.MaterialPartTagGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.PartToSchematicGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.PartTypeTagGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.RepairKitResourceGenerator;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.SchematicPartTagGenerator;
import com.sigmundgranaas.forgero.minecraft.common.item.Attributes;
import com.sigmundgranaas.forgero.minecraft.common.item.DynamicItems;
import com.sigmundgranaas.forgero.minecraft.common.loot.SingleLootEntry;
import com.sigmundgranaas.forgero.minecraft.common.loot.function.LootFunctions;
import com.sigmundgranaas.forgero.minecraft.common.resources.DisassemblyRecipeLoader;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class ForgeroPostInit implements ForgeroInitializedEntryPoint {
	public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

	@Override
	public void onInitialized(StateService service) {
		registerBlocks();
		registerStates(service);
		DynamicItems.registerDynamicItems();
		registerTreasure();
		registerCommands();
		LootFunctions.register();
		Attributes.register();
		disassemblyReloader();

		dataReloader();
		lootConditionReloader();
		lootInjectionReloader();


		registerRecipes();
		registerAARPRecipes(service);
	}

	private void registerTreasure() {
		TreasureInjector.getInstance().registerLoot();
	}

	private void registerCommands() {
		new CommandRegistry().registerCommand();
	}

	private void registerStates(StateService service) {
		var sortingMap = new HashMap<String, Integer>();

		service.all().stream().map(Supplier::get)
				.filter(state -> !state.test(Type.WEAPON) && !state.test(Type.TOOL)).forEach(state -> sortingMap.compute(materialName(state), (key, value) -> value == null || rarity(state) > value ? rarity(state) : value));

		ForgeroStateRegistry.CREATE_STATES.stream()
				.filter(state -> !Registry.ITEM.containsId(new Identifier(ForgeroStateRegistry.STATE_TO_CONTAINER.get(state.get().identifier()))))
				.filter(state -> !Registry.ITEM.containsId(new Identifier(state.get().identifier())))
				.sorted((element1, element2) -> compareStates(element1.get(), element2.get(), sortingMap))
				.forEach(state -> {
					try {
						var converter = StateToItemConverter.of(state);
						Identifier identifier = converter.id();
						var item = converter.convert();
						Registry.register(Registry.ITEM, identifier, item);
					} catch (InvalidIdentifierException e) {
						LOGGER.error("invalid identifier: {}", state.get().identifier());
						LOGGER.error(e);
					}
				});
	}

	private void dataReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public void reload(ResourceManager manager) {
				var config = ForgeroConfigurationLoader.load();
				Set<String> availableDependencies = FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toSet());
				PipelineBuilder.builder()
						.register(() -> config)
						.register(FabricPackFinder.supplier())
						.state(ForgeroStateRegistry.stateListener())
						.register(availableDependencies)
						.silent()
						.build()
						.execute();
			}

			@Override
			public Identifier getFabricId() {
				return new Identifier(ForgeroInitializer.MOD_NAMESPACE, "data");
			}
		});
	}

	private void lootConditionReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public void reload(ResourceManager manager) {
				Conditions.INSTANCE.refresh();
				Gson gson = new Gson();
				for (Resource res : manager.findResources("conditions", path -> path.getPath().endsWith(".json")).values()) {
					try (InputStream stream = res.getInputStream()) {
						ConditionData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), ConditionData.class);
						LootCondition.of(data).ifPresent(Conditions.INSTANCE::register);
					} catch (Exception e) {
						Forgero.LOGGER.error(e);
					}
				}
			}

			@Override
			public Identifier getFabricId() {
				return new Identifier(Forgero.NAMESPACE, "loot_condition");
			}
		});
	}

	private void lootInjectionReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public void reload(ResourceManager manager) {
				Gson gson = new Gson();
				TreasureInjector injector = TreasureInjector.getInstance();
				for (Resource res : manager.findResources("forgero_loot", path -> path.getPath().endsWith(".json")).values()) {
					try (InputStream stream = res.getInputStream()) {
						LootEntryData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), LootEntryData.class);
						injector.registerEntry(data.id(), SingleLootEntry.of(data));
					} catch (Exception e) {
						Forgero.LOGGER.error(e);
					}
				}
				TreasureInjector.getInstance().registerDynamicLoot();
			}

			@Override
			public Identifier getFabricId() {
				return ResourceReloadListenerKeys.LOOT_TABLES;
			}
		});
	}

	private void disassemblyReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public void reload(ResourceManager manager) {
				DisassemblyRecipeLoader.reload(manager);
			}

			@Override
			public Identifier getFabricId() {
				return new Identifier(Forgero.NAMESPACE, "disassembly");
			}
		});
	}

	private void registerBlocks() {
		Registry.register(Registry.BLOCK, ASSEMBLY_STATION, ASSEMBLY_STATION_BLOCK);
		Registry.register(Registry.ITEM, ASSEMBLY_STATION, ASSEMBLY_STATION_ITEM);
		Registry.register(Registry.SCREEN_HANDLER, ASSEMBLY_STATION, ASSEMBLY_STATION_SCREEN_HANDLER);
	}

	private void registerAARPRecipes(StateService service) {
		ARRPGenerator.register(new RepairKitResourceGenerator(ForgeroConfigurationLoader.configuration, service));
		ARRPGenerator.register(() -> new PartToSchematicGenerator(service));
		ARRPGenerator.register(() -> new MaterialPartTagGenerator(service));
		ARRPGenerator.register(() -> new SchematicPartTagGenerator(service));
		ARRPGenerator.register(() -> new PartTypeTagGenerator(service));
		ARRPGenerator.generate(service);
	}

	private void registerRecipes() {
		RecipeRegistry.INSTANCE.registerRecipeSerializers();
	}


	private int getOrderingFromState(Map<String, Integer> map, State state) {
		var name = materialName(state);
		int rarity = (int) state.stream().applyAttribute(AttributeType.RARITY);
		return map.getOrDefault(name, rarity);
	}

	private String materialName(State state) {
		var elements = state.name().split(ELEMENT_SEPARATOR);
		if (elements.length > 1) {
			return elements[0];
		} else {
			return state.name();
		}
	}

	private int compareStates(State element1, State element2, Map<String, Integer> map) {
		int elementOrdering = getOrderingFromState(map, element1) - getOrderingFromState(map, element2);
		int nameOrdering = materialName(element1).compareTo(materialName(element2));

		if (elementOrdering != 0) {
			return elementOrdering;
		} else if (nameOrdering != 0) {
			return nameOrdering;
		} else {
			return rarity(element1) - rarity(element2);
		}
	}

	private int rarity(State state) {
		return (int) state.stream().applyAttribute(AttributeType.RARITY);
	}

}
