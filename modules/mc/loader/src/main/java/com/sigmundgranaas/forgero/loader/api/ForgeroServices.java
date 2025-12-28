package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.item.ItemStack;

import java.util.Optional;

/**
 * The primary service interface for accessing Forgero's core systems.
 * <p>
 * This interface is designed for dependency injection. Code should receive
 * this interface via constructor injection or through the {@link ForgeroInitializedCallback} event.
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Event-Based Access (Recommended)</h3>
 * <pre>{@code
 * public class MyManager {
 *     private static ComponentConverter converter;
 *     private static Resolver resolver;
 *
 *     static {
 *         ForgeroInitializedCallback.EVENT.register(services -> {
 *             converter = services.converter();
 *             resolver = services.resolver();
 *         });
 *     }
 *
 *     public void doSomething(ItemStack stack) {
 *         converter.toComponent(stack).ifPresent(component -> {
 *             // use component...
 *         });
 *     }
 * }
 * }</pre>
 *
 * <h3>Constructor Injection</h3>
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
	 * Returns the slot manager for high-level slot operations.
	 * <p>
	 * Use this to query available slots, check upgrade compatibility,
	 * and install/remove upgrades.
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * SlotManager manager = services.slotManager();
	 *
	 * // Check if gem can be installed
	 * if (manager.canInstall(tool, gem)) {
	 *     InstallationResult result = manager.install(tool, gem);
	 *     if (result.success()) {
	 *         Component upgraded = result.orElseThrow();
	 *     }
	 * }
	 * }</pre>
	 *
	 * @return The slot manager instance
	 */
	SlotManager slotManager();

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
