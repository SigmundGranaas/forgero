package com.sigmundgranaas.forgero.example;

// THIRD-PARTY EXTENSION — public API validation harness.
// Same discipline as ExampleForgeroAddon: imports may come ONLY from common.api,
// common.identifier.api, Minecraft, and the JDK. Authoring + registering custom conditions now
// needs ZERO core.* / *.impl.* imports (this is the F1 win — see docs/forgero-2-public-api-review.md).

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.api.ConditionContext;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.ForgeroCodecs;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * Registers two custom conditions a downstream mod might want, with no internal types in sight:
 *   - mymod:is_top              — true only at the root of the assembly
 *   - mymod:in_offensive_slot   — true only when installed in an offensive-context slot
 *
 * Wire via fabric.mod.json: "entrypoints": { "forgero:data_plugin": ["...ExampleDataPlugin"] }.
 */
public final class ExampleDataPlugin implements DataPlugin {

	/** JSON config for a parameterized condition: {@code {"type":"example:in_slot","slot":"..."}}. */
	public record InSlot(OpenIdentifier slot) {
		static final Codec<InSlot> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						ForgeroCodecs.IDENTIFIER.fieldOf("slot").forGetter(InSlot::slot)
				).apply(instance, InSlot::new));
	}

	@Override
	public void register(PluginRegistrationContext context) {
		// Parameterless conditions — a plain predicate over the structural context.
		context.registerStaticCondition("example:is_top", ConditionContext::isRoot);
		context.registerStaticCondition("example:in_offensive_slot",
				ctx -> ctx.isInSlotType(OpenIdentifier.parse("forgero:contexts/offensive")));

		// Parameterized condition — JSON config parsed with a public codec, still no internal types.
		context.registerStaticCondition("example:in_slot", InSlot.CODEC,
				(cfg, ctx) -> ctx.isInSlotType(cfg.slot()));
	}

	@Override
	public String getId() {
		return "example:data_plugin";
	}
}
