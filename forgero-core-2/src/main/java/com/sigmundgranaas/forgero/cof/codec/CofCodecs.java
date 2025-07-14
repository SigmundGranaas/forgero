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
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.List;
import java.util.Optional;

public class CofCodecs {

	/**
	 * Creates the master codec for {@link CofComponent}. This factory method resolves the
	 * dependency on attribute and feature codecs, which are now created dynamically.
	 *
	 * @param attributeListCodec The fully constructed codec for a list of {@link AttributeData}.
	 * @param featureListCodec   The fully constructed codec for a list of {@link FeatureData}.
	 * @return A complete, recursive codec for {@link CofComponent}.
	 */
	public static Codec<CofComponent> create(Codec<List<AttributeData>> attributeListCodec, Codec<List<FeatureData>> featureListCodec) {

		/**
		 * A private proxy codec to break the recursive static initialization cycle.
		 * This codec is referenced by other codecs, but its delegate is set later.
		 */
		class ComponentDtoCodecProxy implements Codec<CofComponent> {
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
		ComponentDtoCodecProxy recursiveComponentCodec = new ComponentDtoCodecProxy();

		// 2. Define the other DTO codecs using the proxy for recursive parts.
		Codec<CofStructure> structureDtoCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.unboundedMap(CodecConstants.OPEN_IDENTIFIER_CODEC, recursiveComponentCodec).fieldOf("slots").forGetter(CofStructure::slots)
				).apply(instance, CofStructure::new));

		Codec<CofUpgradeSlot> upgradeSlotDtoCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(CofUpgradeSlot::id),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(CofUpgradeSlot::type),
						Codec.STRING.fieldOf("description").forGetter(CofUpgradeSlot::description),
						recursiveComponentCodec.optionalFieldOf("content").forGetter(CofUpgradeSlot::content)
				).apply(instance, CofUpgradeSlot::new));

		Codec<CofUpgrades> upgradesDtoCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.list(upgradeSlotDtoCodec).fieldOf("slots").forGetter(CofUpgrades::slots)
				).apply(instance, CofUpgrades::new));

		// 3. Now, the full component codec can be defined, using the injected dependencies.
		Codec<CofComponent> fullComponentDtoCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(CofComponent::id),
						Codec.STRING.fieldOf("component_type").forGetter(CofComponent::componentType),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).xmap(java.util.Set::copyOf, java.util.List::copyOf).optionalFieldOf("tags").forGetter(dto -> Optional.ofNullable(dto.tags())),
						attributeListCodec.optionalFieldOf("attributes").forGetter(dto -> Optional.ofNullable(dto.attributes())),
						featureListCodec.optionalFieldOf("features").forGetter(dto -> Optional.ofNullable(dto.features())),
						structureDtoCodec.optionalFieldOf("structure").forGetter(dto -> Optional.ofNullable(dto.structure())),
						upgradesDtoCodec.optionalFieldOf("upgrades").forGetter(dto -> Optional.ofNullable(dto.upgrades())),
						Codec.INT.optionalFieldOf("cof_version").forGetter(dto -> Optional.ofNullable(dto.cofVersion()))
				).apply(instance, (id, type, tags, attributes, features, structure, upgrades, version) ->
						new CofComponent(id, type, tags.orElse(null), attributes.orElse(null), features.orElse(null), structure.orElse(null), upgrades.orElse(null), version.orElse(0))
				));

		// 4. Finally, set the delegate in the proxy to the fully constructed codec.
		recursiveComponentCodec.setDelegate(fullComponentDtoCodec);

		return fullComponentDtoCodec;
	}
}
