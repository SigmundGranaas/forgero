package com.sigmundgranaas.forgero.cof.codec;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.cof.ComponentConstructorRegistry;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofStructure;
import com.sigmundgranaas.forgero.cof.dto.CofUpgradeSlot;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentCofCodec implements Codec<Component> {

	private final ComponentRegistry componentRegistry;
	private final List<PropertyCodec<?>> propertyCodecs;
	private final ComponentConstructorRegistry constructorRegistry;
	private final Codec<CofComponent> cofComponentCodec;

	private final Cache<Component, JsonElement> serializationCache;

	public ComponentCofCodec(ComponentRegistry componentRegistry, ComponentConstructorRegistry constructorRegistry, Codec<CofComponent> cofComponentCodec) {
		this.componentRegistry = componentRegistry;
		this.constructorRegistry = constructorRegistry;
		this.propertyCodecs = PropertyRegistry.getInstance().getPropertyCodecs();
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
		List<AttributeData> attributes = new ArrayList<>();
		List<FeatureData> features = new ArrayList<>();

		for (PropertyCodec<?> codec : propertyCodecs) {
			if (codec.getPropertyDataType() == AttributeData.class) {
				@SuppressWarnings("unchecked")
				var typedCodec = (PropertyCodec<AttributeData>) codec;
				component.getProperties().stream().map(typedCodec::toData).filter(Objects::nonNull).forEach(attributes::add);
			} else if (codec.getPropertyDataType() == FeatureData.class) {
				@SuppressWarnings("unchecked")
				var typedCodec = (PropertyCodec<FeatureData>) codec;
				component.getProperties().stream().map(typedCodec::toData).filter(Objects::nonNull).forEach(features::add);
			}
		}

		CofStructure structureDto = null;
		if (component instanceof StructuredComponent structured) {
			Map<OpenIdentifier, CofComponent> slotDtos = structured.structure().slots().values().stream()
					.collect(Collectors.toMap(StructureSlot::id, slot -> this.buildDtoFromComponent(slot.content())));
			structureDto = new CofStructure(slotDtos);
		}

		CofUpgrades upgradesDto = null;
		if (component instanceof CustomizableComponent customizable) {
			List<CofUpgradeSlot> upgradeDtos = customizable.upgrades().slots().stream()
					.map(slot -> new CofUpgradeSlot(slot.id(), slot.type(), slot.description(), slot.content().map(this::buildDtoFromComponent)))
					.toList();
			upgradesDto = new CofUpgrades(upgradeDtos);
		}

		String componentType = constructorRegistry.getTypeString(component);
		return new CofComponent(component.id(), componentType, component.getTags(), attributes, features, structureDto, upgradesDto, 1);
	}

	private DataResult<Component> buildComponentFromDto(CofComponent dto) {
		List<Property> properties = Stream.of(
						buildProperties(dto.attributes(), AttributeData.class),
						buildProperties(dto.features(), FeatureData.class)
				).flatMap(List::stream)
				.toList();

		DataResult<ComponentStructure> structureResult = dto.structure() != null ? buildStructureFromDto(dto.id(), dto.structure()) : DataResult.success(null);
		DataResult<ComponentUpgrades> upgradesResult = dto.upgrades() != null ? buildUpgradesFromDto(dto.id(), dto.upgrades()) : DataResult.success(null);

		return structureResult.flatMap(structure ->
				upgradesResult.flatMap(upgrades ->
						constructorRegistry.construct(dto, properties, structure, upgrades)
								.mapError(err -> "Failed to construct component " + dto.id() + ": " + err)
				)
		);
	}

	private DataResult<ComponentStructure> buildStructureFromDto(OpenIdentifier parentId, CofStructure structureDto) {
		Map<OpenIdentifier, DataResult<Component>> decodedSlots = structureDto.slots().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> this.buildComponentFromDto(entry.getValue())));

		List<String> errors = decodedSlots.values().stream().flatMap(dr -> dr.error().stream()).map(DataResult.PartialResult::message).toList();
		if (!errors.isEmpty()) {
			return DataResult.error(() -> "Errors in structure slots for component " + parentId + ": " + String.join(", ", errors));
		}

		Component pristineParent = componentRegistry.get(parentId).orElse(null);
		if (!(pristineParent instanceof StructuredComponent pristineStructured)) {
			return DataResult.error(() -> "Cannot deserialize structure for a non-structured component: " + parentId);
		}

		Map<OpenIdentifier, StructureSlot> slots = new HashMap<>();
		for (Map.Entry<OpenIdentifier, DataResult<Component>> entry : decodedSlots.entrySet()) {
			Component component = entry.getValue().result().get();
			StructureSlot pristineSlot = pristineStructured.structure().slots().get(entry.getKey());
			if (pristineSlot == null) {
				return DataResult.error(() -> "Structure slot " + entry.getKey() + " not found on pristine component " + parentId);
			}
			slots.put(entry.getKey(), new StructureSlot(entry.getKey(), pristineSlot.type(), pristineSlot.description(), component));
		}
		return DataResult.success(new ComponentStructure(slots));
	}


	private DataResult<ComponentUpgrades> buildUpgradesFromDto(OpenIdentifier parentId, CofUpgrades upgradesDto) {
		Component pristineParent = componentRegistry.get(parentId).orElse(null);
		if (!(pristineParent instanceof CustomizableComponent pristineCustomizable)) {
			return DataResult.error(() -> "Cannot deserialize upgrades for a non-customizable component: " + parentId);
		}

		Map<OpenIdentifier, UpgradeSlot> pristineSlotsById = pristineCustomizable.upgrades().slots().stream()
				.collect(Collectors.toMap(UpgradeSlot::id, Function.identity()));

		List<UpgradeSlot> newSlots = new ArrayList<>();
		for (CofUpgradeSlot slotDto : upgradesDto.slots()) {
			UpgradeSlot pristineSlot = pristineSlotsById.get(slotDto.id());
			if (pristineSlot == null) {
				return DataResult.error(() -> "Upgrade slot " + slotDto.id() + " not found on pristine component " + parentId);
			}

			DataResult<Optional<Component>> contentResult = slotDto.content()
					.map(cofComponent -> buildComponentFromDto(cofComponent).map(Optional::of))
					.orElse(DataResult.success(Optional.empty()));

			if (contentResult.error().isPresent()) {
				return contentResult.map(c -> null);
			}
			newSlots.add(new UpgradeSlot(slotDto.id(), slotDto.type(), slotDto.description(), pristineSlot.validator(), contentResult.result().get()));
		}
		return DataResult.success(new ComponentUpgrades(newSlots));
	}

	@SuppressWarnings("unchecked")
	private <D extends com.sigmundgranaas.forgero.data.loading.api.data.PropertyData> List<Property> buildProperties(List<D> dataList, Class<D> dataType) {
		if (dataList == null || dataList.isEmpty()) {
			return Collections.emptyList();
		}
		return propertyCodecs.stream()
				.filter(codec -> codec.getPropertyDataType() == dataType)
				.findFirst()
				.map(codec -> ((PropertyCodec<D>) codec).build(dataList))
				.orElse(Collections.emptyList());
	}
}
