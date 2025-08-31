package com.sigmundgranaas.forgero.cof.codec;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.cof.ComponentConstructor;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofSlot;
import com.sigmundgranaas.forgero.cof.dto.CofStructure;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComponentCofCodec implements Codec<Component> {

	private final ComponentRegistry componentRegistry;
	private final ComponentConstructor constructorRegistry;
	private final Codec<CofComponent> cofComponentCodec;

	private final Cache<Component, JsonElement> serializationCache;

	public ComponentCofCodec(ComponentRegistry componentRegistry, ComponentConstructor constructorRegistry, Codec<CofComponent> cofComponentCodec) {
		this.componentRegistry = componentRegistry;
		this.constructorRegistry = constructorRegistry;
		this.cofComponentCodec = cofComponentCodec;
		this.serializationCache = Caffeine.newBuilder()
				.maximumSize(1000)
				.expireAfterAccess(5, TimeUnit.MINUTES)
				.build();
	}

	@Override
	public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> ops, T input) {
		// Try to decode as a string (ID-based) first.
		Optional<String> idString = ops.getStringValue(input).result();
		if (idString.isPresent()) {
			OpenIdentifier id = CodecConstants.IDENTIFIER_FACTORY.of(idString.get());
			return componentRegistry.get(id)
					.map(comp -> DataResult.success(Pair.of(comp, ops.empty())))
					.orElse(DataResult.error(() -> "No component found in registry for ID: " + id));
		}

		// If it's not a string, assume it's a full object.
		return cofComponentCodec.decode(ops, input).flatMap(pair -> {
			CofComponent dto = pair.getFirst();
			return buildComponentFromDto(dto).map(comp -> Pair.of(comp, pair.getSecond()));
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> DataResult<T> encode(Component input, DynamicOps<T> ops, T prefix) {
		Component pristineComponent = componentRegistry.get(input.id()).orElse(null);

		// If the component is identical to the one in the registry, serialize as ID.
		if (input.equals(pristineComponent)) {
			return Codec.STRING.encode(input.id().toString(), ops, prefix);
		}

		// Check cache if using JsonOps
		if (ops == JsonOps.INSTANCE || ops == JsonOps.COMPRESSED) {
			JsonElement cached = serializationCache.getIfPresent(input);
			if (cached != null) {
				return DataResult.success((T) cached);
			}
		}

		// Otherwise, perform full serialization.
		CofComponent dto = buildDtoFromComponent(input);
		DataResult<T> encodedResult = cofComponentCodec.encode(dto, ops, prefix);

		// Store in cache if using JsonOps and successful
		if ((ops == JsonOps.INSTANCE || ops == JsonOps.COMPRESSED) && encodedResult.result().isPresent()) {
			serializationCache.put(input, (JsonElement) encodedResult.result().get());
		}

		return encodedResult;
	}

	private CofComponent buildDtoFromComponent(Component component) {
		Map<String, List<?>> properties = component.propertiesAsMap();

		CofStructure structureDto = null;
		if (component instanceof StructuredComponent structured) {
			Map<OpenIdentifier, CofSlot> slotDtos = structured.structure().slots().values().stream()
					.collect(Collectors.toMap(
							StructureSlot::id,
							slot -> new CofSlot(slot.id(), slot.type(), slot.description(), buildDtoFromComponent(slot.content()), null)
					));
			structureDto = new CofStructure(slotDtos);
		}

		CofUpgrades upgradesDto = null;
		if (component instanceof CustomizableComponent customizable) {
			List<CofSlot> upgradeDtos = customizable.upgrades().slots().stream()
					.map(slot -> new CofSlot(
							slot.id(),
							slot.type(),
							slot.description(),
							slot.content().map(this::buildDtoFromComponent).orElse(null),
							null // validTags are part of pristine definition, not serialized
					))
					.toList();
			upgradesDto = new CofUpgrades(upgradeDtos);
		}

		OpenIdentifier componentType = constructorRegistry.getTypeIdentifier(component);
		return new CofComponent(component.id(), componentType, component.getTags(), properties, structureDto, upgradesDto, 1);
	}

	private DataResult<Component> buildComponentFromDto(CofComponent dto) {
		return constructorRegistry.construct(
						dto.id(),
						dto.componentType(),
						dto.tags(),
						dto.properties(),
						buildStructureFromDto(dto).orElse(null),
						buildUpgradesFromDto(dto).orElse(null))
				.mapError(err -> "Failed to construct component " + dto.id() + ": " + err);
	}


	private Optional<ComponentStructure> buildStructureFromDto(CofComponent parentDto) {
		if (parentDto.structure() == null) {
			return Optional.empty();
		}

		Component pristineParent = componentRegistry.get(parentDto.id()).orElse(null);
		if (!(pristineParent instanceof StructuredComponent pristineStructured)) {
			// This can happen if a component is created entirely from NBT, without a backing definition.
			// In this case, we have to trust the DTO.
			return Optional.empty(); // Or attempt to build without pristine info, which is complex.
		}

		Map<OpenIdentifier, StructureSlot> slots = new HashMap<>();
		for (CofSlot slotDto : parentDto.structure().slots().values()) {
			if (slotDto.content() == null) continue;

			var componentResult = buildComponentFromDto(slotDto.content());
			if (componentResult.error().isPresent()) return Optional.empty(); // Propagate error

			StructureSlot pristineSlot = pristineStructured.structure().slots().get(slotDto.id());
			if (pristineSlot == null) continue; // Slot not found on pristine, skip.

			slots.put(slotDto.id(), new StructureSlot(slotDto.id(), pristineSlot.type(), pristineSlot.description(), componentResult.result().get()));
		}
		return Optional.of(new ComponentStructure(slots));
	}


	private Optional<ComponentUpgrades> buildUpgradesFromDto(CofComponent parentDto) {
		if (parentDto.upgrades() == null) {
			return Optional.empty();
		}
		Component pristineParent = componentRegistry.get(parentDto.id()).orElse(null);
		if (!(pristineParent instanceof CustomizableComponent pristineCustomizable)) {
			return Optional.empty();
		}

		Map<OpenIdentifier, UpgradeSlot> pristineSlotsById = pristineCustomizable.upgrades().slots().stream()
				.collect(Collectors.toMap(UpgradeSlot::id, Function.identity()));

		List<UpgradeSlot> newSlots = new ArrayList<>();
		for (CofSlot slotDto : parentDto.upgrades().slots()) {
			UpgradeSlot pristineSlot = pristineSlotsById.get(slotDto.id());
			if (pristineSlot == null) continue;

			var contentResult = Optional.ofNullable(slotDto.content())
					.map(this::buildComponentFromDto)
					.map(dr -> dr.map(Optional::of))
					.orElse(DataResult.success(Optional.empty()));

			if (contentResult.error().isPresent()) return Optional.empty();

			newSlots.add(new UpgradeSlot(slotDto.id(), slotDto.type(), slotDto.description(), pristineSlot.validator(), contentResult.result().get()));
		}
		return Optional.of(new ComponentUpgrades(newSlots));
	}
}
