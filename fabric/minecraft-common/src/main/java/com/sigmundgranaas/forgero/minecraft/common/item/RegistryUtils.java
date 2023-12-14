package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;

import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RegistryUtils {
	public static  <T>  void register(GenericRegistry<T> registry, Supplier<Registerable<T>> entry){
		entry.get().register(registry);
	}

	public static <T>  void register(GenericRegistry<T> registry, Registerable<T> entry){
		entry.register(registry);
	}


	public static BiFunction<StateProvider, Item.Settings, Item> itemClassPreparer(TriFunction<StateProvider,Item.Settings, DynamicToolItemSettings,Item> converter){
		return  (state, settings) -> dynamicTool(converter, state, settings);
	}


	public static SettingProcessor settingProcessor(GenericRegistry<SettingProcessor> registry){
		return (Item.Settings settings, State state) -> {
			registry.values().forEach(processor -> processor.apply(settings, state));
			return settings;
		};
	}

	public static Item dynamicTool(TriFunction<StateProvider,Item.Settings, DynamicToolItemSettings,Item> converter, StateProvider provider, Item.Settings settings){
		DynamicToolItemSettings params = createDynamicSettings(provider);
		return converter.apply(provider, settings, params);
	}

	public static Predicate<StateProvider> typeMatcher(Type type){
		return (state ) -> state.get().type().test(type);
	};

	public static Predicate<StateProvider> typeMatcher(String type){
		return (state ) -> state.get().type().test(Type.of(type));
	};


	public static Ingredient createIngredientFromState(StateProvider provider){
		var state = provider.get();

		Optional<State> ingredientState = Optional.empty();

		if (state instanceof ConstructedTool tool) {
			if (tool.getHead() instanceof MaterialBased based) {
				ingredientState = Optional.of(based.baseMaterial());
			}
		}

		StateService service = StateService.INSTANCE;
		return ingredientState
				.flatMap(service::convert)
				.map(Ingredient::ofStacks)
				.orElse(ToolMaterials.WOOD.getRepairIngredient());
	}

	public static DynamicToolItemSettings createDynamicSettings(StateProvider provider) {
		var state = provider.get();

		int attackDamage = (int) state.stream().applyAttribute(AttackDamage.KEY);

		float attackSpeed = state.stream().applyAttribute(AttackDamage.KEY);

		Ingredient ingredient = createIngredientFromState(provider);

		return new DynamicToolItemSettings(attackDamage, attackSpeed, ingredient);
	}

	public record DynamicToolItemSettings(int attackDamage, float attackSpeed, Ingredient ingredient){}

	public static final BiFunction<StateProvider, Item.Settings ,Item> defaultItem = (state, settings) -> new DefaultStateItem(settings, state);

	public static RankableConverter<StateProvider, ItemGroup> typeConverter(Type type, ItemGroup group) {
		return typeConverter(type, group, 1);
	}

	public static RankableConverter<StateProvider, ItemGroup> typeConverter(Type type, ItemGroup group, int priority) {
		return BuildableGenericConverter.<StateProvider, ItemGroup>builder()
				.matcher(state -> state.get().test(type))
				.priority(priority)
				.converter(state -> group)
				.build();
	}
}
