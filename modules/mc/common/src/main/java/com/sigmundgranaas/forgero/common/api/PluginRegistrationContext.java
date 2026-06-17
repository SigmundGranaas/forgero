package com.sigmundgranaas.forgero.common.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface PluginRegistrationContext {
	/**
	 * Register an item creator for a specific item class identifier.
	 */
	void registerItemCreator(String itemClass, ItemCreator creator);

	/**
	 * Registers a custom static condition as a plain predicate over the public
	 * {@link ConditionContext} — the ergonomic path for conditions that carry no JSON data.
	 *
	 * <p>An addon can author and register a condition without touching any internal type:
	 * <pre>{@code
	 * context.registerStaticCondition("mymod:is_top", ConditionContext::isRoot);
	 * context.registerStaticCondition("mymod:in_offensive_slot",
	 *         ctx -> ctx.isInSlotType(OpenIdentifier.parse("forgero:contexts/offensive")));
	 * }</pre>
	 *
	 * Conditions that need to parse JSON fields use the {@code Codec}-based overloads instead.
	 *
	 * @param type      The unique type identifier for the condition (e.g., "mymod:is_top").
	 * @param predicate The condition logic, evaluated against the structural context.
	 */
	default void registerStaticCondition(String type, Predicate<ConditionContext> predicate) {
		OpenIdentifier id = OpenIdentifier.parse(type);
		StaticCondition condition = new StaticCondition() {
			@Override
			public boolean test(ResolutionContext context) {
				return predicate.test(ConditionContext.of(context));
			}

			@Override
			public OpenIdentifier type() {
				return id;
			}
		};
		registerStaticConditionCodec(type, Codec.unit(condition));
	}

	/**
	 * Registers a custom static condition that parses JSON config — still without any internal
	 * type. The addon supplies a {@link Codec} for its own config record (use
	 * {@link ForgeroCodecs#IDENTIFIER} for identifier fields) and a predicate over (config,
	 * {@link ConditionContext}):
	 * <pre>{@code
	 * record InTag(OpenIdentifier tag) {}
	 * Codec<InTag> codec = RecordCodecBuilder.create(i -> i.group(
	 *         ForgeroCodecs.IDENTIFIER.fieldOf("tag").forGetter(InTag::tag)).apply(i, InTag::new));
	 * context.registerStaticCondition("mymod:in_tagged_slot", codec,
	 *         (cfg, ctx) -> ctx.isInSlotType(cfg.tag()));
	 * }</pre>
	 *
	 * @param type        The unique type identifier for the condition.
	 * @param configCodec Codec for the condition's JSON config.
	 * @param predicate   The condition logic over (config, structural context).
	 * @param <C>         The config record type.
	 */
	default <C> void registerStaticCondition(String type, Codec<C> configCodec, java.util.function.BiPredicate<C, ConditionContext> predicate) {
		OpenIdentifier id = OpenIdentifier.parse(type);
		Codec<FunctionalStaticCondition<C>> codec = configCodec.xmap(
				config -> new FunctionalStaticCondition<>(id, config, predicate),
				FunctionalStaticCondition::config);
		registerStaticConditionCodec(type, codec);
	}

	/**
	 * Registers a codec for a custom static condition.
	 * @param type The unique type identifier for the condition (e.g., "forgero:at_depth").
	 * @param factory The codec for parsing the condition.
	 */
	void registerStaticConditionCodec(String type, Function<Supplier<TagResolver>, Codec<? extends StaticCondition>> factory);

	/**
	 * Registers a codec for a custom static condition.
	 * @param type The unique type identifier for the condition (e.g., "forgero:at_depth").
	 * @param codec The codec for parsing the condition.
	 */
	void registerStaticConditionCodec(String type, Codec<? extends StaticCondition> codec);

	/**
	 * Registers a codec for a custom dynamic condition.
	 * @param type The unique type identifier for the condition (e.g., "forgero:target_has_tag").
	 * @param codec The codec for parsing the condition.
	 */
	void registerDynamicConditionCodec(String type, Codec<? extends DynamicCondition> codec);

	/**
	 * Registers a builder function for a custom property codec.
	 * The function will be invoked by the data loader with a supplier for the master ConditionCodec,
	 * allowing properties to correctly parse their own conditional blocks.
	 *
	 * @param key          The JSON key for the property (e.g., "forgero:attributes").
	 * @param codecBuilder A function that takes a ConditionCodec supplier and returns a complete codec for your property list.
	 */
	void registerPropertyCodec(PropertyKey<?> key, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>> codecBuilder);

	/**
	 * Registers a codec for a custom slot type.
	 * This enables plugins to add new slot implementations (e.g., ArrowSlot, SoulSlot).
	 *
	 * @param type  The unique type identifier for the slot (e.g., "forgero:arrow", "forgero:soul").
	 * @param codec The codec for parsing the slot.
	 */
	void registerSlotCodec(String type, Codec<? extends com.sigmundgranaas.forgero.core.component.api.Slot> codec);

	/**
	 * Registers a factory that builds a custom slot kind from data, so the kind can be authored
	 * directly in part templates (the load-time counterpart to {@link #registerSlotCodec}). Register
	 * both for a slot kind that is authorable <em>and</em> persists: the factory builds it at load,
	 * the codec (de)serialises it at runtime.
	 *
	 * @param kind    The slot kind identifier (its {@code Slot.type()}, e.g. "forgero:potion").
	 * @param factory Builds a Slot of this kind from a parsed slot definition.
	 */
	void registerSlotFactory(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier kind,
	                         com.sigmundgranaas.forgero.core.component.api.slot.SlotFactory factory);
}
