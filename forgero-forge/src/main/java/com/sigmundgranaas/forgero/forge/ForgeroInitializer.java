package com.sigmundgranaas.forgero.forge;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.forge.pack.ForgePackFinder;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;


@Mod(Forgero.NAMESPACE)
public class ForgeroInitializer {

	public static DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Forgero.NAMESPACE);

	public ForgeroInitializer() {
		IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

		var pipeline = PipelineBuilder
				.builder()
				.register(ForgeroConfigurationLoader::load)
				.register(new ForgePackFinder())
				.state(ForgeroStateRegistry.stateListener())
				.state(ForgeroStateRegistry.compositeListener())
				.createStates(ForgeroStateRegistry.createStateListener())
				.inflated(ForgeroStateRegistry.constructListener())
				.inflated(ForgeroStateRegistry.containerListener())
				.recipes(ForgeroStateRegistry.recipeListener())
				.build();

		pipeline.execute();

		ITEM_REGISTRY.register(MOD_BUS);
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
