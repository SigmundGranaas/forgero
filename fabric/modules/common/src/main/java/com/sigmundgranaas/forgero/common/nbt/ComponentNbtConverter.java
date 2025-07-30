package com.sigmundgranaas.forgero.common.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.cof.ComponentConstructorRegistry;
import com.sigmundgranaas.forgero.cof.codec.CofCodecs;
import com.sigmundgranaas.forgero.cof.codec.ComponentCofCodec;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodec;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ComponentNbtConverter {
	public static final String FORGERO_NBT_KEY = "ForgeroComponent";
	private static ComponentNbtConverter INSTANCE;

	private final Codec<Component> componentCodec;

	private ComponentNbtConverter(Codec<Component> componentCodec) {
		this.componentCodec = componentCodec;
	}

	public static void initialize(ComponentRegistry componentRegistry) {
		if (INSTANCE == null) {
			ComponentConstructorRegistry.getInstance().registerCoreTypes();

			Map<String, Codec<? extends StaticCondition>> staticConditionCodecs = new HashMap<>();
			staticConditionCodecs.put("forgero:self_has_tag", TagMatchCondition.CODEC);
			Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs = new HashMap<>();
			ConditionCodec conditionCodec = new ConditionCodec(staticConditionCodecs, dynamicConditionCodecs);
			Codec<List<AttributeData>> attributeListCodec = Codec.list(AttributeCodecs.create(conditionCodec));
			Codec<List<FeatureData>> featureListCodec = FeatureCodecs.createFeatureDataListCodec()
					;

			Codec<CofComponent> cofComponentCodec = CofCodecs.create(attributeListCodec, featureListCodec);

			ComponentCofCodec componentCodec = new ComponentCofCodec(
					componentRegistry,
					ComponentConstructorRegistry.getInstance(),
					cofComponentCodec
			);

			INSTANCE = new ComponentNbtConverter(componentCodec);
		}
	}

	public static ComponentNbtConverter getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("ComponentNbtConverter has not been initialized. Call initialize() first.");
		}
		return INSTANCE;
	}

	public Optional<Component> fromNbt(NbtCompound nbt) {
		if (nbt == null || !nbt.contains(FORGERO_NBT_KEY)) {
			return Optional.empty();
		}
		NbtElement forgeroNbt = nbt.get(FORGERO_NBT_KEY);

		return componentCodec.decode(NbtOps.INSTANCE, forgeroNbt)
				.resultOrPartial(err -> LoggerFactory.getLogger(ComponentNbtConverter.class).error("Failed to decode component from NBT: {}", err))
				.map(Pair::getFirst);
	}

	public NbtCompound toNbt(Component component) {
		NbtCompound nbt = new NbtCompound();
		componentCodec.encodeStart(NbtOps.INSTANCE, component)
				.resultOrPartial(err -> LoggerFactory.getLogger(ComponentNbtConverter.class).error("Failed to encode component to NBT: {}", err))
				.ifPresent(forgeroNbt -> nbt.put(FORGERO_NBT_KEY, forgeroNbt));
		return nbt;
	}
}
