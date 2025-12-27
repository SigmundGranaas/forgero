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
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComponentCofCodec implements Codec<Component> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentCofCodec.class);

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
			Map<OpenIdentifier, CofSlot> slotDtos = structured.structure().slots().all().stream()
					.collect(Collectors.toMap(
							StructureSlot::id,
							slot -> new CofSlot(slot.id(), slot.type(), slot.description(), buildDtoFromComponent(slot.content()), null)
					));
			structureDto = new CofStructure(slotDtos);
		}

		CofUpgrades upgradesDto = null;
		if (component instanceof CustomizableComponent customizable) {
			List<CofSlot> upgradeDtos = customizable.upgrades().slots().all().stream()
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

		OpenIdentifier componentType = component.getTypeIdentifier();
		return new CofComponent(
				component.id(),
				componentType,
				Optional.of(component.getTags()),
				Optional.of(properties),
				Optional.ofNullable(structureDto),
				Optional.ofNullable(upgradesDto),
				Optional.of(1)
		);
	}

	private DataResult<Component> buildComponentFromDto(CofComponent dto) {
		LOGGER.debug("Building component from DTO: id={}, type={}, hasStructure={}, hasUpgrades={}",
				dto.id(), dto.componentType(), dto.structure().isPresent(), dto.upgrades().isPresent());

		var structure = buildStructureFromDto(dto).orElse(null);
		var upgrades = buildUpgradesFromDto(dto).orElse(null);

		LOGGER.debug("Built structure: {}, upgrades: {} for component {}",
				structure != null, upgrades != null, dto.id());

		return constructorRegistry.construct(
						dto.id(),
						dto.componentType(),
						dto.tags().orElse(Set.of()),
						dto.properties().orElse(Map.of()),
						structure,
						upgrades)
				.mapError(err -> "Failed to construct component " + dto.id() + ": " + err);
	}


	private Optional<ComponentStructure> buildStructureFromDto(CofComponent parentDto) {
		if (parentDto.structure().isEmpty()) {
			return Optional.empty();
		}

		Component pristineParent = componentRegistry.get(parentDto.id()).orElse(null);
		if (!(pristineParent instanceof StructuredComponent pristineStructured)) {
			return Optional.empty();
		}

		CofStructure structure = parentDto.structure().get();
		List<StructureSlot> slots = new ArrayList<>();
		for (CofSlot slotDto : structure.slots().values()) {
			if (slotDto.content() == null) continue;

			var componentResult = buildComponentFromDto(slotDto.content());
			if (componentResult.error().isPresent()) return Optional.empty();

			Optional<StructureSlot> pristineSlotOpt = pristineStructured.structure().get(slotDto.id());
			if (pristineSlotOpt.isEmpty()) continue;

			StructureSlot pristineSlot = pristineSlotOpt.get();
			slots.add(new StructureSlot(slotDto.id(), pristineSlot.type(), pristineSlot.description(),
					pristineSlot.validator(), componentResult.result().get()));
		}
		return Optional.of(ComponentStructure.of(slots));
	}


	private Optional<ComponentUpgrades> buildUpgradesFromDto(CofComponent parentDto) {
		boolean typeRequiresUpgrades = com.sigmundgranaas.forgero.cof.ComponentTypeRegistry.requiresUpgrades(parentDto.componentType());

		if (parentDto.upgrades().isEmpty()) {
			if (typeRequiresUpgrades) {
				LOGGER.warn("No upgrades DTO for component {} (type: {}) but type requires upgrades. Creating empty upgrades.",
						parentDto.id(), parentDto.componentType());
				return Optional.of(ComponentUpgrades.empty());
			}
			LOGGER.debug("No upgrades DTO for component {}", parentDto.id());
			return Optional.empty();
		}

		CofUpgrades upgrades = parentDto.upgrades().get();

		Component pristineParent = componentRegistry.get(parentDto.id()).orElse(null);
		if (pristineParent == null) {
			LOGGER.warn("No pristine component found in registry for id: {} (type: {}). Building upgrades from DTO without validation.",
					parentDto.id(), parentDto.componentType());
			return buildUpgradesFromDtoWithoutPristine(parentDto);
		}

		if (!(pristineParent instanceof CustomizableComponent pristineCustomizable)) {
			LOGGER.warn("Pristine component {} is not CustomizableComponent (actual type: {}). Building upgrades from DTO without validation.",
					parentDto.id(), pristineParent.getClass().getSimpleName());
			return buildUpgradesFromDtoWithoutPristine(parentDto);
		}

		LOGGER.debug("Building upgrades for component {} with {} upgrade slots from DTO",
				parentDto.id(), upgrades.slots().size());

		Map<OpenIdentifier, UpgradeSlot> pristineSlotsById = pristineCustomizable.upgrades().slots().all().stream()
				.collect(Collectors.toMap(UpgradeSlot::id, Function.identity()));

		List<UpgradeSlot> newSlots = new ArrayList<>();
		for (CofSlot slotDto : upgrades.slots()) {
			UpgradeSlot pristineSlot = pristineSlotsById.get(slotDto.id());
			if (pristineSlot == null) {
				LOGGER.debug("Skipping upgrade slot {} - not found in pristine component {}", slotDto.id(), parentDto.id());
				continue;
			}

			var contentResult = Optional.ofNullable(slotDto.content())
					.map(this::buildComponentFromDto)
					.map(dr -> dr.map(Optional::of))
					.orElse(DataResult.success(Optional.empty()));

			if (contentResult.error().isPresent()) {
				LOGGER.error("Failed to build upgrade content for slot {} in component {}: {}",
						slotDto.id(), parentDto.id(), contentResult.error().get());
				return Optional.empty();
			}

			newSlots.add(new UpgradeSlot(slotDto.id(), slotDto.type(), slotDto.description(),
					pristineSlot.validator(), contentResult.result().get()));
		}

		LOGGER.debug("Successfully built {} upgrade slots for component {}", newSlots.size(), parentDto.id());
		return Optional.of(ComponentUpgrades.of(newSlots));
	}


	private Optional<ComponentUpgrades> buildUpgradesFromDtoWithoutPristine(CofComponent parentDto) {
		if (parentDto.upgrades().isEmpty() || parentDto.upgrades().get().slots().isEmpty()) {
			LOGGER.debug("Creating empty upgrades for component {} (no pristine definition)", parentDto.id());
			return Optional.of(ComponentUpgrades.empty());
		}

		CofUpgrades upgrades = parentDto.upgrades().get();
		LOGGER.debug("Building {} upgrade slots from DTO without pristine definition for component {}",
				upgrades.slots().size(), parentDto.id());

		List<UpgradeSlot> newSlots = new ArrayList<>();
		for (CofSlot slotDto : upgrades.slots()) {
			var contentResult = Optional.ofNullable(slotDto.content())
					.map(this::buildComponentFromDto)
					.map(dr -> dr.map(Optional::of))
					.orElse(DataResult.success(Optional.empty()));

			if (contentResult.error().isPresent()) {
				LOGGER.error("Failed to build upgrade content for slot {} in component {} (no pristine): {}",
						slotDto.id(), parentDto.id(), contentResult.error().get());
				return Optional.empty();
			}

			newSlots.add(new UpgradeSlot(
					slotDto.id(),
					slotDto.type(),
					slotDto.description(),
					SlotValidator.ACCEPT_ALL,
					contentResult.result().get()
			));
		}

		LOGGER.debug("Successfully built {} upgrade slots from DTO without pristine for component {}",
				newSlots.size(), parentDto.id());
		return Optional.of(ComponentUpgrades.of(newSlots));
	}
}
