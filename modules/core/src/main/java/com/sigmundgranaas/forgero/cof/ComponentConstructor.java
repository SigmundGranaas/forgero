package com.sigmundgranaas.forgero.cof;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A service responsible for constructing component instances from their constituent parts
 * and for determining the type identifier of a component instance for serialization.
 * <p>
 * This class is immutable and its logic is defined by the mappings provided via its constructor,
 * making it suitable for dependency injection.
 */
public final class ComponentConstructor {

	private final Map<OpenIdentifier, ConstructorFunction> constructorFunctions;
	private final List<Pair<Class<? extends Component>, OpenIdentifier>> typeIdentifiers;

	/**
	 * Constructs a new constructor service with specified logic.
	 *
	 * @param constructorFunctions A map from a type identifier to a function that can construct that component.
	 * @param typeIdentifiers      An ordered list mapping a component class to its type identifier.
	 *                             The list must be ordered from most-specific to least-specific class to ensure
	 *                             correct type resolution for serialization.
	 */
	public ComponentConstructor(Map<OpenIdentifier, ConstructorFunction> constructorFunctions, List<Pair<Class<? extends Component>, OpenIdentifier>> typeIdentifiers) {
		this.constructorFunctions = Map.copyOf(constructorFunctions);
		this.typeIdentifiers = List.copyOf(typeIdentifiers);
	}

	/**
	 * Creates a new ComponentConstructor instance populated with all the default Forgero component types.
	 * This is the standard factory for obtaining a fully configured constructor.
	 *
	 * @return A new, pre-configured instance.
	 */
	public static ComponentConstructor defaults() {
		Map<OpenIdentifier, ConstructorFunction> constructors = new LinkedHashMap<>();
		List<Pair<Class<? extends Component>, OpenIdentifier>> types = new ArrayList<>();

		// A helper to register a new type, ensuring both maps are populated correctly.
		// Registration order matters for the typeIdentifiers list, so we add most specific classes first.
		TriConsumer<OpenIdentifier, Class<? extends Component>, ConstructorFunction> register = (id, clazz, constructor) -> {
			constructors.put(id, constructor);
			types.add(Pair.of(clazz, id));
		};

		// Register from most specific to least specific to ensure correct type resolution.
		register.accept(id("structured_extensible_equipment"), StructuredExtensibleEquipment.class, (id, tags, props, struct, upgs) -> {
			if (struct == null || upgs == null) {
				return DataResult.error(() -> "Missing structure or upgrades for structured_extensible_equipment with id: " + id);
			}
			return DataResult.success(new StructuredExtensibleEquipment(id, tags, props, struct, upgs));
		});

		register.accept(id("structured_extensible_part"), StructuredExtensiblePart.class, (id, tags, props, struct, upgs) -> {
			if (struct == null || upgs == null) {
				return DataResult.error(() -> "Missing structure or upgrades for structured_extensible_part with id: " + id);
			}
			return DataResult.success(new StructuredExtensiblePart(id, tags, props, struct, upgs));
		});

		register.accept(id("structured_equipment"), StructuredEquipment.class, (id, tags, props, struct, upgs) -> {
			if (struct == null) {
				return DataResult.error(() -> "Missing structure for structured_equipment with id: " + id);
			}
			return DataResult.success(new StructuredEquipment(id, tags, props, struct));
		});

		register.accept(id("structured_part"), StructuredPart.class, (id, tags, props, struct, upgs) -> {
			if (struct == null) {
				return DataResult.error(() -> "Missing structure for structured_part with id: " + id);
			}
			return DataResult.success(new StructuredPart(id, tags, props, struct));
		});

		register.accept(id("extensible_equipment"), ExtensibleEquipment.class, (id, tags, props, struct, upgs) -> {
			if (upgs == null) {
				return DataResult.error(() -> "Missing upgrades for extensible_equipment with id: " + id);
			}
			return DataResult.success(new ExtensibleEquipment(id, tags, props, upgs));
		});

		register.accept(id("extensible_part"), ExtensiblePart.class, (id, tags, props, struct, upgs) -> {
			if (upgs == null) {
				return DataResult.error(() -> "Missing upgrades for extensible_part with id: " + id);
			}
			return DataResult.success(new ExtensiblePart(id, tags, props, upgs));
		});

		register.accept(id("static_equipment"), StaticEquipment.class, (id, tags, props, struct, upgs) ->
				DataResult.success(new StaticEquipment(id, tags, props))
		);

		register.accept(id("static_component"), StaticComponent.class, (id, tags, props, struct, upgs) ->
				DataResult.success(new StaticComponent(id, tags, props))
		);

		return new ComponentConstructor(constructors, types);
	}

	/**
	 * Finds the type identifier for a given component instance.
	 * It iterates through the registered types and returns the ID of the first class
	 * that the component is an instance of.
	 *
	 * @param component The component instance.
	 * @return The corresponding type identifier.
	 * @throws IllegalArgumentException if no matching type is found.
	 */
	public OpenIdentifier getTypeIdentifier(Component component) {
		return typeIdentifiers.stream()
				.filter(pair -> pair.getFirst().isInstance(component))
				.findFirst()
				.map(Pair::getSecond)
				.orElseThrow(() -> new IllegalArgumentException("Unsupported component type for serialization: " + component.getClass().getName()));
	}

	/**
	 * Constructs a Component instance from its constituent parts.
	 *
	 * @param id          The ID of the component to create.
	 * @param type        The type identifier of the component.
	 * @param tags        The tags of the component.
	 * @param properties  The fully parsed properties of the component.
	 * @param structure   The component's structure, if any.
	 * @param upgrades    The component's upgrades, if any.
	 * @return A DataResult containing the new Component or an error message.
	 */
	public DataResult<Component> construct(OpenIdentifier id, OpenIdentifier type, Set<OpenIdentifier> tags, Map<String, List<?>> properties, @Nullable ComponentStructure structure, @Nullable ComponentUpgrades upgrades) {
		ConstructorFunction constructor = constructorFunctions.get(type);
		if (constructor == null) {
			return DataResult.error(() -> "Unknown component type '" + type + "' for id: " + id);
		}
		Set<OpenIdentifier> nonNullTags = Optional.ofNullable(tags).orElse(Collections.emptySet());
		return constructor.apply(id, nonNullTags, properties, structure, upgrades);
	}

	private static OpenIdentifier id(String path) {
		return CodecConstants.IDENTIFIER_FACTORY.of(path);
	}

	/**
	 * A functional interface for a component construction function.
	 */
	@FunctionalInterface
	public interface ConstructorFunction {
		DataResult<Component> apply(OpenIdentifier id, Set<OpenIdentifier> tags, Map<String, List<?>> properties, @Nullable ComponentStructure structure, @Nullable ComponentUpgrades upgrades);
	}

	/**
	 * A helper functional interface for internal use in the static factory method.
	 */
	@FunctionalInterface
	private interface TriConsumer<T, U, V> {
		void accept(T t, U u, V v);
	}
}
