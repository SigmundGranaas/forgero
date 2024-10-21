package com.sigmundgranaas.forgero.bow;

import static com.sigmundgranaas.forgero.bow.Attributes.*;
import static com.sigmundgranaas.forgero.core.api.identity.ModificationRuleBuilder.builder;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.*;

import java.util.List;

import com.sigmundgranaas.forgero.bow.handler.ChargeCrossbowHandler;
import com.sigmundgranaas.forgero.bow.handler.LaunchCrossBowArrowHandler;
import com.sigmundgranaas.forgero.bow.handler.LoadCrossBowArrowHandler;
import com.sigmundgranaas.forgero.bow.item.DynamicCrossBowItemRegistrationHandler;
import com.sigmundgranaas.forgero.bow.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.bow.predicate.ChargedPredicate;
import com.sigmundgranaas.forgero.core.api.identity.Condition;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleBuilder;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.handler.HandlerBuilderRegistry;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.registry.RegistryFactory;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UsageTickHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UseHandler;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipAttributeRegistry;

public class ForgeroCrossBowInitializer implements ForgeroPreInitializationEntryPoint {

	public static Condition crossbowStockType = Condition.type(Type.CROSSBOW_STOCK);

	public static ModificationRuleBuilder crossbowStock = builder()
			.when(crossbowStockType)
			.ignore();

	public static Condition crossBowLimbType = Condition.type(Type.CROSSBOW_LIMB);

	public static ModificationRuleBuilder crossbowLimb = builder()
			.when(crossBowLimbType)
			.replaceElement("crossbow_limb", "crossbow");

	@Override
	public void onPreInitialization() {
		var settingRegistry = ItemRegistries.SETTING_PROCESSOR;
		var groupRegistry = ItemRegistries.GROUP_CONVERTER;

		var factory = new RegistryFactory<>(groupRegistry);

		var baseConverter = BuildableStateConverter.builder()
				.group(factory::convert)
				.settings(settingProcessor(settingRegistry))
				.item(defaultItem)
				.priority(0)
				.build();

		register(ItemRegistries.STATE_CONVERTER, new DynamicCrossBowItemRegistrationHandler(baseConverter));

		var bows = List.of(DRAW_POWER, DRAW_SPEED, ACCURACY, Weight.KEY, Durability.KEY);

		TooltipAttributeRegistry.filterBuilder()
				.type(Type.CROSSBOW_STOCK)
				.attributes(bows)
				.register();

		TooltipAttributeRegistry.filterBuilder()
				.type(Type.CROSSBOW_LIMB)
				.attributes(bows)
				.register();

		ModificationRuleRegistry modification = ModificationRuleRegistry.staticRegistry();

		modification.registerRule("forgero:crossbow_stock", crossbowStock.build());
		modification.registerRule("forgero:crossbow_limb", crossbowLimb.build());

		HandlerBuilderRegistry.register(UseHandler.KEY, LaunchCrossBowArrowHandler.TYPE, LaunchCrossBowArrowHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, LoadCrossBowArrowHandler.TYPE, LoadCrossBowArrowHandler.BUILDER);
		HandlerBuilderRegistry.register(UsageTickHandler.KEY, ChargeCrossbowHandler.TYPE, ChargeCrossbowHandler.BUILDER);

		PredicateFactory.register(ChargedPredicate.ChargedPredicateBuilder::new);

	}
}

