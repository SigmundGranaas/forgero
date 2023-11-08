package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.TypeSpecificPredicate;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityPredicate {
	public static final EntityPredicate ANY = EntityPredicate.builder()
			.type(EntityTypePredicate.ANY)
			.distance(DistancePredicate.ANY)
			.location(LocationPredicate.ANY)
			.steppingOn(LocationPredicate.ANY)
			.effects(EntityEffectPredicate.EMPTY)
			.nbt(NbtPredicate.ANY)
			.flags(EntityFlagsPredicate.ANY)
			.equipment(EntityEquipmentPredicate.ANY)
			.typeSpecific(TypeSpecificPredicate.ANY)
			.build();

	@Setter
	@Builder.Default
	private EntityTypePredicate type = EntityTypePredicate.ANY;
	@Setter
	@Builder.Default
	private DistancePredicate distance = DistancePredicate.ANY;
	@Setter
	@Builder.Default
	private LocationPredicate location = LocationPredicate.ANY;
	@Setter
	@Builder.Default
	private LocationPredicate steppingOn = LocationPredicate.ANY;
	@Setter
	@Builder.Default
	private EntityEffectPredicate effects = EntityEffectPredicate.EMPTY;
	@Setter
	@Builder.Default
	private NbtPredicate nbt = NbtPredicate.ANY;
	@Setter
	@Builder.Default
	private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
	@Setter
	@Builder.Default
	private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
	@Setter
	@Builder.Default
	private TypeSpecificPredicate typeSpecific = TypeSpecificPredicate.ANY;
	@Setter
	@Builder.Default
	private EntityPredicate vehicle = EntityPredicate.ANY;
	@Setter
	@Builder.Default
	private EntityPredicate passenger = EntityPredicate.ANY;
	@Setter
	@Builder.Default
	private EntityPredicate targetedEntity = EntityPredicate.ANY;
	@Setter
	@Nullable
	private String team;

	public static EntityPredicate fromJson(@Nullable JsonElement json) {
		if (json != null && !json.isJsonNull()) {
			JsonObject jsonObject = JsonHelper.asObject(json, "entity");
			EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonObject.get("type"));
			DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
			LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
			LocationPredicate locationPredicate2 = LocationPredicate.fromJson(jsonObject.get("stepping_on"));
			EntityEffectPredicate entityEffectPredicate = EntityEffectPredicate.fromJson(jsonObject.get("effects"));
			NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
			EntityFlagsPredicate entityFlagsPredicate = EntityFlagsPredicate.fromJson(jsonObject.get("flags"));
			EntityEquipmentPredicate entityEquipmentPredicate = EntityEquipmentPredicate.fromJson(jsonObject.get("equipment"));
			TypeSpecificPredicate typeSpecificPredicate = TypeSpecificPredicate.fromJson(jsonObject.get("type_specific"));
			EntityPredicate entityPredicate = fromJson(jsonObject.get("vehicle"));
			EntityPredicate entityPredicate2 = fromJson(jsonObject.get("passenger"));
			EntityPredicate entityPredicate3 = fromJson(jsonObject.get("targeted_entity"));
			String string = JsonHelper.getString(jsonObject, "team", null);
			return new EntityPredicate.EntityPredicateBuilder()
					.type(entityTypePredicate)
					.distance(distancePredicate)
					.location(locationPredicate)
					.steppingOn(locationPredicate2)
					.effects(entityEffectPredicate)
					.nbt(nbtPredicate)
					.flags(entityFlagsPredicate)
					.equipment(entityEquipmentPredicate)
					.typeSpecific(typeSpecificPredicate)
					.team(string)
					.vehicle(entityPredicate)
					.passenger(entityPredicate2)
					.targetedEntity(entityPredicate3)
					.build();
		} else {
			return ANY;
		}
	}

	public boolean test(World world, @Nullable Vec3d pos, @Nullable Entity entity) {
		if (this == ANY) {
			return true;
		}

		if (entity == null) {
			return false;
		}

		if (!this.type.matches(entity.getType())) {
			return false;
		}

		if (pos == null && this.distance != DistancePredicate.ANY) {
			return false;
		}

		if (pos != null && !this.distance.test(pos.x, pos.y, pos.z, entity.getX(), entity.getY(), entity.getZ())) {
			return false;
		}

		if (!this.location.test(world, entity.getX(), entity.getY(), entity.getZ())) {
			return false;
		}

		if (this.steppingOn != LocationPredicate.ANY) {
			Vec3d vec3d = Vec3d.ofCenter(entity.getSteppingPos());
			if (!this.steppingOn.test(world, vec3d.getX(), vec3d.getY(), vec3d.getZ())) {
				return false;
			}
		}

		if (!this.effects.test(entity) || !this.nbt.test(entity) || !this.flags.test(entity) || !this.equipment.test(entity)) {
			return false;
		}

		if (!this.vehicle.test(world, pos, entity.getVehicle())) {
			return false;
		}

		if (this.passenger != ANY && entity.getPassengerList().stream().noneMatch(e -> this.passenger.test(world, pos, e))) {
			return false;
		}

		if (!this.targetedEntity.test(world, pos, entity instanceof MobEntity ? ((MobEntity) entity).getTarget() : null)) {
			return false;
		}

		if (this.team != null) {
			AbstractTeam abstractTeam = entity.getScoreboardTeam();
			return abstractTeam != null && this.team.equals(abstractTeam.getName());
		}

		return true;
	}
}
