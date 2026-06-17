package com.sigmundgranaas.forgero.cof.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofSlot;
import com.sigmundgranaas.forgero.cof.dto.CofStructure;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CofCodecs {

	/**
	 * Creates the master codec for {@link CofComponent}. This factory method resolves the
	 * dependency on the property map codec, which is now created dynamically.
	 *
	 * @param propertyMapCodec The fully constructed codec for the properties map.
	 * @return A complete, recursive codec for {@link CofComponent}.
	 */
	public static Codec<CofComponent> create(Codec<Map<String, List<?>>> propertyMapCodec) {

		// A private proxy codec to break the recursive static initialization cycle.
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

		// 2. Define the unified Slot DTO codec using the proxy for recursive content.
		Codec<CofSlot> slotCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(CofSlot::id),
						// Slot type is a tag-like classifier (e.g. materials/roles/upgrade_material),
						// not a registry id — preserve its full path so it does not mutate across a
						// serialize/load round-trip and stays comparable to the template-loaded form.
						CodecConstants.TAG_IDENTIFIER_CODEC.fieldOf("type").forGetter(CofSlot::type),
						Codec.STRING.optionalFieldOf("description").forGetter(slot -> Optional.ofNullable(slot.description())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(slot -> Optional.ofNullable(slot.tags())),
						recursiveComponentCodec.optionalFieldOf("content").forGetter(CofSlot::contentOpt),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("valid_tags").forGetter(slot -> Optional.ofNullable(slot.validTags())),
						// Optional slot kind (Slot.type()); absent => the standard component-upgrade slot.
						CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("kind").forGetter(slot -> Optional.ofNullable(slot.kind()))
				).apply(instance, (id, type, desc, tags, content, validTags, kind) -> new CofSlot(id, type, desc.orElse(null), tags.orElse(null), content.orElse(null), validTags.orElse(null), kind.orElse(null)))
		);

		// 3. Define the other DTO codecs using the slot codec.
		Codec<CofStructure> structureDtoCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.unboundedMap(CodecConstants.OPEN_IDENTIFIER_CODEC, slotCodec).fieldOf("slots").forGetter(CofStructure::slots)
				).apply(instance, CofStructure::new));

		Codec<CofUpgrades> upgradesDtoCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.list(slotCodec).fieldOf("slots").forGetter(CofUpgrades::slots)
				).apply(instance, CofUpgrades::new));

		// 4. Now, the full component codec can be defined.
		Codec<CofComponent> fullComponentDtoCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(CofComponent::id),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("component_type").forGetter(CofComponent::componentType),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).xmap(java.util.Set::copyOf, java.util.List::copyOf).optionalFieldOf("tags").forGetter(CofComponent::tags),
						propertyMapCodec.optionalFieldOf("properties").forGetter(CofComponent::properties),
						structureDtoCodec.optionalFieldOf("structure").forGetter(CofComponent::structure),
						upgradesDtoCodec.optionalFieldOf("upgrades").forGetter(CofComponent::upgrades),
						Codec.INT.optionalFieldOf("cof_version", 1).forGetter(dto -> dto.cofVersion().orElse(1))
				).apply(instance, (id, type, tags, properties, structure, upgrades, version) ->
						new CofComponent(id, type, tags, properties, structure, upgrades, Optional.of(version))
				));

		// 5. Finally, set the delegate in the proxy to the fully constructed codec.
		recursiveComponentCodec.setDelegate(fullComponentDtoCodec);

		return fullComponentDtoCodec;
	}
}
