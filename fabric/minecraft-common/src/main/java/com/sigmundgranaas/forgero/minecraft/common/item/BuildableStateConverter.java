package com.sigmundgranaas.forgero.minecraft.common.item;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import lombok.Builder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;

@Builder(toBuilder = true)
public class BuildableStateConverter implements RankableConverter<StateProvider, ItemData> {
	@Builder.Default
	private final Function<StateProvider, ItemGroup> group = state -> ItemGroups.getGroups().get(0);

	@Builder.Default
	private final SettingProcessor settings = (settings, state) -> settings;

	@Builder.Default
	private final Predicate<StateProvider> matcher = state -> true;

	@Builder.Default
	private final Function<StateProvider, Identifier> identifier = state -> new Identifier(state.get().identifier());

	@Builder.Default
	private final int priority = 0;

	@Builder.Default
	private final BiFunction<StateProvider, Item.Settings, Item> item = (state, settings) -> new DefaultStateItem(settings, state);

	public ItemData convert(StateProvider state) {
		ItemGroup group = this.group.apply(state);
		Item.Settings settings = this.settings.apply(new Item.Settings(), state.get());
		Item item = this.item.apply(state, settings);
		Identifier id = identifier.apply(state);
		return new ItemData(item, id, settings, group);
	}

	@Override
	public boolean matches(StateProvider state) {
		return matcher.test(state);
	}

	@Override
	public int priority() {
		return priority;
	}
}
