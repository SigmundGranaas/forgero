package com.sigmundgranaas.forgero.bow.item;

import static com.sigmundgranaas.forgero.bow.ForgeroBowInitializer.FORGERO_BOWS;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeConverter;

import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.registry.Registerable;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.type.Type;

import net.minecraft.item.ItemGroup;

public class BowGroupRegistrars implements Registerable<RankableConverter<StateProvider, ItemGroup>> {
	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemGroup>> registry) {
		registry.register("forgero:bow", typeConverter(Type.BOW, FORGERO_BOWS, 3));
		registry.register("forgero:bow_limb", typeConverter(Type.BOW_LIMB, FORGERO_BOWS, 3));
		registry.register("forgero:arrow", typeConverter(Type.ARROW, FORGERO_BOWS, 3));
		registry.register("forgero:arrow_head", typeConverter(Type.ARROW_HEAD, FORGERO_BOWS, 3));
	}
}
