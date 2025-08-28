package com.sigmundgranaas.forgero.loader.plugin;

import com.sigmundgranaas.forgero.core.condition.predicate.*;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.common.property.bettercombat.BetterCombatIdentifierProperty;
import com.sigmundgranaas.forgero.common.property.namereplacement.NameReplacementProperty;
import com.sigmundgranaas.forgero.common.property.tooltip.TooltipProperty;

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
		context.registerStaticConditionCodec("forgero:in_slot_type",  InSlotTypeCondition.CODEC);
		context.registerStaticConditionCodec("forgero:is_root", IsRootCondition.CODEC);
		context.registerStaticConditionCodec("forgero:slot_contains", SlotContainsCondition.CODEC);

		// The factory for TagMatchCondition.codec is now registered.
		context.registerStaticConditionCodec("forgero:self_has_tag", TagMatchCondition::codec);
		context.registerStaticConditionCodec("forgero:root_has_tag", TagMatchCondition::codec);
	}

	@Override
	public String getId() {
		return "forgero-defaults-properties";
	}
}
