package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.item.ItemStack;

import java.util.Optional;

/**
 * The primary service interface for accessing Forgero's core systems.
 * <p>
 * This interface is designed for dependency injection. Internal code should receive
 * this interface via constructor injection or through the {@link ForgeroInitializedCallback} event.
 * <p>
 * For external mod developers who prefer static access, see {@link ForgeroApi} which
 * provides convenient static methods wrapping this interface.
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Internal Code (Recommended)</h3>
 * <pre>{@code
 * public class MyService {
 *     private final ForgeroServices services;
 *
 *     public MyService(ForgeroServices services) {
 *         this.services = services;
 *     }
 *
 *     public void doSomething() {
 *         TagResolver resolver = services.tagResolver();
 *         // use resolver...
 *     }
 * }
 * }</pre>
 *
 * <h3>Event-Based Access</h3>
 * <pre>{@code
 * ForgeroInitializedCallback.EVENT.register(services -> {
 *     TagResolver resolver = services.tagResolver();
 *     // initialize your systems with the resolver...
 * });
 * }</pre>
 *
 * @see ForgeroApi for static convenience methods
 * @see ForgeroInitializedCallback for event-based initialization
 */
public interface ForgeroServices {

	/**
	 * Returns the tag resolver for querying tag relationships and inheritance.
	 * <p>
	 * Use this to check if components have tags, find tagged items, and traverse
	 * the tag hierarchy.
	 *
	 * @return The tag resolver instance
	 */
	TagResolver tagResolver();

	/**
	 * Returns the component converter for Forgero/Minecraft conversions.
	 * <p>
	 * This is the primary way to convert between {@link Component} and {@link ItemStack}.
	 *
	 * @return The component converter instance
	 */
	ComponentConverter converter();

	/**
	 * Returns the property resolver for computing attributes and features.
	 *
	 * @return The property resolver instance
	 */
	Resolver resolver();

	/**
	 * Returns the component registry containing all loaded default-state components.
	 *
	 * @return The component registry instance
	 */
	ComponentRegistry componentRegistry();

	/**
	 * Returns the tagged registry for tag-based component queries.
	 * <p>
	 * This provides powerful querying capabilities combining the component registry
	 * with tag inheritance.
	 *
	 * @return The tagged component registry
	 */
	TaggedRegistry<Component> taggedComponents();

	/**
	 * Returns the NBT converter for serializing and deserializing components.
	 *
	 * @return The NBT converter instance
	 */
	ComponentNbtConverter nbtConverter();

	/**
	 * Maps an ItemStack to its corresponding Component.
	 * <p>
	 * This is a convenience method equivalent to {@code converter().toComponent(stack)}.
	 *
	 * @param stack The ItemStack to convert
	 * @return The component if the stack represents a Forgero item, empty otherwise
	 */
	Optional<Component> component(ItemStack stack);
}
