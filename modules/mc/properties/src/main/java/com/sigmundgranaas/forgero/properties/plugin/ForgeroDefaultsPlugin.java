package com.sigmundgranaas.forgero.properties.plugin;

import com.sigmundgranaas.forgero.core.condition.predicate.*;
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.common.tooltip.TooltipDescriptor;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;
import com.sigmundgranaas.forgero.properties.property.bettercombat.BetterCombatIdentifierProperty;
import com.sigmundgranaas.forgero.properties.property.namereplacement.NameReplacementProperty;
import com.sigmundgranaas.forgero.properties.property.tooltip.TooltipProperty;

/**
 * A Forgero data plugin that registers all the default custom properties and conditions.
 */
public class ForgeroDefaultsPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		registerPropertyCodecs(context);
		registerConditionCodecs(context);
	}

	private void registerPropertyCodecs(PluginRegistrationContext context) {
		// Register the tooltip descriptor compile pass so terminals pre-compile their
		// descriptors at construction, read O(1) by TooltipBuilder.
		CompilerPasses.register(TooltipDescriptor.KEY, TooltipDescriptor.Engine::new);

		context.registerPropertyCodec(
				TooltipProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(TooltipProperty.codec(conditionCodecSupplier.get()))
		);

		context.registerPropertyCodec(
				NameReplacementProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(NameReplacementProperty.codec(conditionCodecSupplier.get()))
		);

		context.registerPropertyCodec(
				BetterCombatIdentifierProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(BetterCombatIdentifierProperty.codec(conditionCodecSupplier.get()))
		);
	}

	private void registerConditionCodecs(PluginRegistrationContext context) {
		// Conditions with no dependencies ignore the supplier.
		context.registerStaticConditionCodec("forgero:at_depth", AtDepthCondition.CODEC);
		context.registerStaticConditionCodec("forgero:has_sibling", HasSiblingCondition.CODEC);
		context.registerStaticConditionCodec("forgero:has_other_contributor", HasOtherContributorCondition.CODEC);
		context.registerStaticConditionCodec("forgero:id_match", IdMatchCondition.CODEC);
		context.registerStaticConditionCodec("forgero:in_slot_type",  InSlotTypeCondition.CODEC);
		context.registerStaticConditionCodec("forgero:is_root", IsRootCondition.CODEC);
		context.registerStaticConditionCodec("forgero:slot_contains", SlotContainsCondition.CODEC);

		// The factory for TagMatchCondition.codec is now registered.
		context.registerStaticConditionCodec("forgero:self_has_tag", TagMatchCondition::codec);
		context.registerStaticConditionCodec("forgero:root_has_tag", TagMatchCondition::codec);

		// Multi-tag conditions for multi-axis taxonomy (Phase 3)
		context.registerStaticConditionCodec("forgero:self_has_all_tags", AllTagsMatchCondition::codec);
		context.registerStaticConditionCodec("forgero:root_has_all_tags", AllTagsMatchCondition::codec);
		context.registerStaticConditionCodec("forgero:self_has_any_tag", AnyTagMatchCondition::codec);
		context.registerStaticConditionCodec("forgero:root_has_any_tag", AnyTagMatchCondition::codec);
	}

	@Override
	public String getId() {
		return "forgero-defaults-properties";
	}
}
