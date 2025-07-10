package com.sigmundgranaas.forgero.cof.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofStructure;
import com.sigmundgranaas.forgero.cof.dto.CofUpgradeSlot;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;

import java.util.Optional;

public class CofCodecs {

	/**
	 * A private proxy codec to break the recursive static initialization cycle.
	 * This codec is referenced by other codecs, but its delegate is set later in a static block.
	 */
	private static class ComponentDtoCodecProxy implements Codec<CofComponent> {
		private Codec<CofComponent> delegate;

		void setDelegate(Codec<CofComponent> delegate) {
			this.delegate = delegate;
		}

		@Override
		public <T> DataResult<Pair<CofComponent, T>> decode(DynamicOps<T> ops, T input) {
			if (delegate == null) {
				return DataResult.error(() -> "Recursive Component DTO Codec not initialized");
			}
			return delegate.decode(ops, input);
		}

		@Override
		public <T> DataResult<T> encode(CofComponent input, DynamicOps<T> ops, T prefix) {
			if (delegate == null) {
				return DataResult.error(() -> "Recursive Component DTO Codec not initialized");
			}
			return delegate.encode(input, ops, prefix);
		}
	}

	// 1. Instantiate the proxy first. It can be referenced immediately.
	private static final ComponentDtoCodecProxy RECURSIVE_COMPONENT_CODEC = new ComponentDtoCodecProxy();

	// 2. Define the other DTO codecs using the proxy for recursive parts.
	public static final Codec<CofStructure> STRUCTURE_DTO_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.unboundedMap(CodecConstants.OPEN_IDENTIFIER_CODEC, RECURSIVE_COMPONENT_CODEC).fieldOf("slots").forGetter(CofStructure::slots)
			).apply(instance, CofStructure::new));

	public static final Codec<CofUpgradeSlot> UPGRADE_SLOT_DTO_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(CofUpgradeSlot::id),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(CofUpgradeSlot::type),
					Codec.STRING.fieldOf("description").forGetter(CofUpgradeSlot::description),
					RECURSIVE_COMPONENT_CODEC.optionalFieldOf("content").forGetter(CofUpgradeSlot::content)
			).apply(instance, CofUpgradeSlot::new));

	public static final Codec<CofUpgrades> UPGRADES_DTO_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.list(UPGRADE_SLOT_DTO_CODEC).fieldOf("slots").forGetter(CofUpgrades::slots)
			).apply(instance, CofUpgrades::new));

	// 3. Now, the full component codec can be defined, as its dependencies are all valid.
	public static final Codec<CofComponent> FULL_COMPONENT_DTO_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(CofComponent::id),
					Codec.STRING.fieldOf("component_type").forGetter(CofComponent::componentType),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).xmap(java.util.Set::copyOf, java.util.List::copyOf).optionalFieldOf("tags").forGetter(dto -> Optional.ofNullable(dto.tags())),
					AttributeCodecs.ATTRIBUTE_DATA_LIST_CODEC.optionalFieldOf("attributes").forGetter(dto -> Optional.ofNullable(dto.attributes())),
					FeatureCodecs.FEATURE_DATA_LIST_CODEC.optionalFieldOf("features").forGetter(dto -> Optional.ofNullable(dto.features())),
					STRUCTURE_DTO_CODEC.optionalFieldOf("structure").forGetter(dto -> Optional.ofNullable(dto.structure())),
					UPGRADES_DTO_CODEC.optionalFieldOf("upgrades").forGetter(dto -> Optional.ofNullable(dto.upgrades())),
					Codec.INT.optionalFieldOf("cof_version").forGetter(dto -> Optional.ofNullable(dto.cofVersion()))
			).apply(instance, (id, type, tags, attributes, features, structure, upgrades, version) ->
					new CofComponent(id, type, tags.orElse(null), attributes.orElse(null), features.orElse(null), structure.orElse(null), upgrades.orElse(null), version.orElse(0))
			));

	// 4. Finally, set the delegate in the proxy to the fully constructed codec.
	static {
		RECURSIVE_COMPONENT_CODEC.setDelegate(FULL_COMPONENT_DTO_CODEC);
	}
}
