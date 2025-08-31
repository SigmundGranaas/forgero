package com.sigmundgranaas.forgero.predicate.minecraft.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.item.EquipmentPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LocationPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record EntityPredicate(
		// Targeting
		Optional<Target> target,

		// Grouped Predicates
		Optional<EntityType<?>> entityType,
		Optional<EntityFlagPredicate> flags,
		Optional<EntityStatsPredicate> stats,
		Optional<EquipmentPredicate> equipment,
		Optional<StatusEffectPredicate> effects,
		Optional<LocationPredicate> location,
		Optional<RelationalPredicate> relational
) implements DynamicCondition {

	public static final OpenIdentifier TYPE = new OpenIdentifier("minecraft", "entity");

	public enum Target {SELF, TARGET_ENTITY}

	@SuppressWarnings("RedundantCast")
	private static final Codec<EntityType<?>> ENTITY_TYPE_CODEC =
			(Codec<EntityType<?>>) (Codec<?>) Identifier.CODEC.xmap(Registries.ENTITY_TYPE::get, Registries.ENTITY_TYPE::getId);

	public static final Codec<EntityPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.xmap(s -> Target.valueOf(s.toUpperCase()), Enum::name).optionalFieldOf("target").forGetter(EntityPredicate::target),
					ENTITY_TYPE_CODEC.optionalFieldOf("type").forGetter(EntityPredicate::entityType),
					EntityFlagPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags),
					EntityStatsPredicate.CODEC.optionalFieldOf("stats").forGetter(EntityPredicate::stats),
					EquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment),
					StatusEffectPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects),
					LocationPredicate.CODEC.optionalFieldOf("location").forGetter(EntityPredicate::location),
					RelationalPredicate.CODEC.optionalFieldOf("relational").forGetter(EntityPredicate::relational)
			).apply(instance, EntityPredicate::new)
	);

	@Override
	public boolean test(DynamicContext context) {
		Optional<Entity> entityToTest = getEntityToTest(context);
		if (entityToTest.isEmpty()) {
			return false;
		}
		Entity entity = entityToTest.get();

		boolean typeMatch = entityType.map(t -> entity.getType() == t).orElse(true);
		boolean flagMatch = flags.map(p -> p.test(entity)).orElse(true);
		boolean statsMatch = stats.map(p -> p.test(entity)).orElse(true);
		boolean locationMatch = location.map(loc -> loc.test(entity.getWorld(), entity.getBlockPos())).orElse(true);
		boolean relationalMatch = relational.map(p -> p.test(context)).orElse(true);
		boolean equipmentMatch = equipment.map(p -> entity instanceof LivingEntity living && p.test(living)).orElse(true);
		boolean effectsMatch = effects.map(p -> entity instanceof LivingEntity living && p.test(living)).orElse(true);

		return typeMatch && flagMatch && statsMatch && equipmentMatch && effectsMatch && locationMatch && relationalMatch;
	}

	private Optional<Entity> getEntityToTest(DynamicContext context) {
		if (target().isPresent() && target().get() == Target.TARGET_ENTITY) {
			return context.get(MinecraftContextKeys.TARGET_ENTITY);
		} else {
			return context.get(MinecraftContextKeys.ENTITY);
		}
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
