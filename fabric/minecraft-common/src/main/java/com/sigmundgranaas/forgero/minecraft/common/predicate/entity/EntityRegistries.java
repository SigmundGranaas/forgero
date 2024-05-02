package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.SpecificationRegistry;

import net.minecraft.entity.Entity;

public class EntityRegistries {
	public static final SpecificationRegistry<Predicate<Entity>> ENTITY_FLAG_PREDICATE_REGISTRY = new SpecificationRegistry<>();
	public static final SpecificationRegistry<Codec<KeyPair<Predicate<Entity>>>> ENTITY_CODEC_REGISTRY = new SpecificationRegistry<>();
}
