package com.sigmundgranaas.forgero.minecraft.common.predicate.world;

import static com.sigmundgranaas.forgero.minecraft.common.api.v0.predicate.Registries.WORLD_CODEC_REGISTRY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.predicate.GroupEntry;
import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.SpecificationBackedPredicateCodec;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class WorldPredicate implements Predicate<World>, Matchable {
	private final GroupEntry<KeyPair<Predicate<World>>> entry;
	private static final String type = "minecraft:world";
	public static final MapCodec<GroupEntry<KeyPair<Predicate<World>>>> WORLD_CODEC = new SpecificationBackedPredicateCodec<>(type, WORLD_CODEC_REGISTRY);
	public static final Codec<WorldPredicate> CODEC = new MapCodec.MapCodecCodec<>(WORLD_CODEC.xmap(WorldPredicate::new, worldPredicate -> worldPredicate.entry));

	public WorldPredicate(GroupEntry<KeyPair<Predicate<World>>> entry) {
		this.entry = entry;
	}

	@Override
	public boolean test(World entity) {
		return entry.entries().stream()
				.map(KeyPair::value)
				.allMatch(specification -> specification.test(entity));
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var entity = context.get(WORLD).or(() -> context.get(ENTITY).map(Entity::getWorld))
				.map(this::test)
				.orElse(false);

		if (entity) {
			return true;
		}
		return context.get(ENTITY_TARGET).map(Entity::getWorld)
				.map(this::test)
				.orElse(false);
	}
}
