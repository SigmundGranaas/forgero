package com.sigmundgranaas.forgero.core.condition;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ResourceRegistry;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.ConstantFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Conditions implements ResourceRegistry<LootCondition> {

	public static final Conditions INSTANCE = new Conditions(new HashMap<>());
	public static String BROKEN_TYPE_KEY = "forgero:broken";
	public static final ClassKey<Feature> BROKEN_KEY = Feature.key(BROKEN_TYPE_KEY);
	public static String UNBREAKABLE_TYPE_KEY = "forgero:unbreakable";
	public static final ClassKey<Feature> UNBREAKABLE_KEY = new ClassKey<>(UNBREAKABLE_TYPE_KEY, Feature.class);

	public static NamedCondition BROKEN = new NamedCondition("broken", Forgero.NAMESPACE, List.of(new ConstantFeature(BROKEN_TYPE_KEY)));
	public static SimpleCondition UNBREAKABLE = new SimpleCondition(List.of(new ConstantFeature(UNBREAKABLE_TYPE_KEY)));
	private final Map<String, LootCondition> conditionMap;

	public Conditions(Map<String, LootCondition> conditionMap) {
		this.conditionMap = conditionMap;
	}

	public Optional<NamedCondition> of(String id) {
		if (id.equals(BROKEN.identifier())) {
			return Optional.of(BROKEN);
		}
		return Optional.ofNullable(conditionMap.get(id));
	}

	public void register(LootCondition condition) {
		conditionMap.put(condition.identifier(), condition);
	}

	@Override
	public Optional<LootCondition> get(String id) {
		return Optional.ofNullable(conditionMap.get(id));
	}

	@Override
	public ImmutableList<LootCondition> get(Type type) {
		return ImmutableList.<LootCondition>builder().build();
	}

	@Override
	public ImmutableList<LootCondition> all() {
		return conditionMap.values().stream().collect(ImmutableList.toImmutableList());
	}

	public void refresh() {
		this.conditionMap.clear();
	}
}
