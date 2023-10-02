package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY_TARGET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriterBuilder;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.WriterHelper;

import net.minecraft.entity.Entity;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.TypeSpecificPredicate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public record EntityPredicateMatcher(EntityPredicate predicate, String variant) implements Matchable {
	public static String ID = "minecraft:entity_properties";
	public static String ID_TARGET = "minecraft:target_entity_properties";


	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		Optional<Entity> playerOpt = context.get(ENTITY);

		if (playerOpt.isEmpty()) {
			return false;
		}

		Entity entity = playerOpt.get();

		if (variant.equals(ID) && predicate.test(entity.getWorld(), entity.getPos(), entity)) {
			return true;
		}

		if (variant.equals(ID_TARGET)) {
			return context.get(ENTITY_TARGET)
					.map(target -> predicate.test(entity.getWorld(), target.getPos(), target))
					.orElse(false);
		}
		return false;
	}

	public static class EntityPredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.or(() -> ElementParser.fromIdentifiedElement(element, ID_TARGET))
					.map(json -> {
						if (json.isJsonObject() && json.getAsJsonObject().has("predicate")) {
							return json.getAsJsonObject("predicate");
						} else {
							return json;
						}
					})
					.map(EntityPredicate::fromJson)
					.map(entity -> new EntityPredicateMatcher(entity, element.getAsJsonObject().get("condition").getAsString()));
		}
	}

	public static class EntityPredicateWriter implements PredicateWriter {
		private final EntityPredicateMatcher predicate;
		private final WriterHelper helper;

		public EntityPredicateWriter(EntityPredicateMatcher predicate, WriterHelper helper) {
			this.predicate = predicate;
			this.helper = helper;
		}

		public static PredicateWriterBuilder builder() {
			return (Matchable matchable, TooltipConfiguration configuration) -> {
				if (matchable instanceof EntityPredicateMatcher entityPredicate) {
					return Optional.of(new EntityPredicateWriter(entityPredicate, new WriterHelper(configuration.toBuilder().baseIndent(configuration.baseIndent() + 2).build())));
				}
				return Optional.empty();
			};
		}

		public static String formatTag(String base, Identifier id) {
			return base + "." + id.getNamespace() + "." + id.getPath();
		}

		public List<MutableText> write(Matchable matchable) {
			List<MutableText> tooltips = new ArrayList<>();

			// Handle Entity Type Predicate
			if (predicate.variant.equals(ID_TARGET) && isNotAny(predicate.predicate.getType())) {
				EntityTypePredicate entityTypePred = predicate.predicate.getType();
				String tag = formatEntityTypeTag(entityTypePred);
				addTooltips("tooltip.forgero.against", tag, tooltips);
			}

			// Handle Entity Type Predicate
			if (predicate.variant.equals(ID) && isNotAny(predicate.predicate.getType())) {
				EntityTypePredicate entityTypePred = predicate.predicate.getType();
				String tag = formatEntityTypeTag(entityTypePred);
				addTooltips("tooltip.forgero.is", tag, tooltips);
			}

			// Handle Location Predicate
			if (isNotAny(predicate.predicate.getLocation())) {
				LocationPredicate locationPred = predicate.predicate.getLocation();
				String tag = formatLocationTag(locationPred);
				addTooltips("tooltip.forgero.in", tag, tooltips);
			}

			// Handle Stepping On Predicate
			if (isNotAny(predicate.predicate.getSteppingOn())) {
				LocationPredicate steppingOnPred = predicate.predicate.getSteppingOn();
				String tag = formatLocationTag(steppingOnPred);
				addTooltips("tooltip.forgero.on", tag, tooltips);
			}

			// Handle Entity Effect Predicate
			if (isNotAny(predicate.predicate.getEffects())) {
				// Assuming EntityEffectPredicate has a way to represent its content as a String or Identifier.
				String tag = formatEffectTag(predicate.predicate.getEffects());
				addTooltips("tooltip.forgero.when", tag, tooltips);
			}

			// Handle NBT Predicate
			if (isNotAny(predicate.predicate.getNbt())) {
				// NBT might be complex, so just indicating presence might be enough
				tooltips.add(helper.writeBase().append(Text.translatable("tooltip.forgero.with_nbt").formatted(Formatting.GRAY)));
			}

			// Handle flags
			if (isNotAny(predicate.predicate.getFlags())) {
				EntityFlagsPredicate flagsPred = predicate.predicate.getFlags();
				List<MutableText> flagTooltips = formatFlagsTag(flagsPred);
				tooltips.addAll(flagTooltips);
			}

			// Handle Equipment Predicate
			if (isNotAny(predicate.predicate.getEquipment())) {
				String tag = formatEquipmentTag(predicate.predicate.getEquipment());
				addTooltips("tooltip.forgero.wearing", tag, tooltips);
			}

			// Handle Type Specific Predicate
			if (isNotAny(predicate.predicate.getTypeSpecific())) {
				// Similar to NBT, might be complex. Indicating presence can be a simpler way.
				tooltips.add(helper.writeBase().append(Text.translatable("tooltip.forgero.type_specific").formatted(Formatting.GRAY)));
			}

			return tooltips.isEmpty() ? Collections.emptyList() : tooltips;
		}

		private String formatEffectTag(EntityEffectPredicate effects) {
			return "effect.minecraft." + effects.toString();
		}

		private List<MutableText> formatFlagsTag(EntityFlagsPredicate flags) {
			List<MutableText> flagTooltips = new ArrayList<>();
			JsonObject jsonFlags = flags.toJson().getAsJsonObject();

			if (jsonFlags.has("is_on_fire") && jsonFlags.get("is_on_fire").getAsBoolean()) {
				flagTooltips.add(Text.translatable("tooltip.forgero.on_fire").formatted(Formatting.GRAY));
			}
			if (jsonFlags.has("is_sneaking") && jsonFlags.get("is_sneaking").getAsBoolean()) {
				flagTooltips.add(Text.translatable("tooltip.forgero.sneaking").formatted(Formatting.GRAY));
			}
			if (jsonFlags.has("is_sprinting") && jsonFlags.get("is_sprinting").getAsBoolean()) {
				flagTooltips.add(Text.translatable("tooltip.forgero.sprinting").formatted(Formatting.GRAY));
			}
			if (jsonFlags.has("is_swimming") && jsonFlags.get("is_swimming").getAsBoolean()) {
				flagTooltips.add(Text.translatable("tooltip.forgero.swimming").formatted(Formatting.GRAY));
			}
			if (jsonFlags.has("is_baby") && jsonFlags.get("is_baby").getAsBoolean()) {
				flagTooltips.add(Text.translatable("tooltip.forgero.baby").formatted(Formatting.GRAY));
			}

			return flagTooltips;
		}

		private String formatEquipmentTag(EntityEquipmentPredicate equipment) {
			return "equipment.minecraft." + equipment.toString();
		}

		private boolean isNotAny(Object predicate) {
			return predicate != EntityTypePredicate.ANY &&
					predicate != LocationPredicate.ANY &&
					predicate != EntityEffectPredicate.EMPTY &&
					predicate != NbtPredicate.ANY &&
					predicate != EntityFlagsPredicate.ANY &&
					predicate != EntityEquipmentPredicate.ANY &&
					predicate != TypeSpecificPredicate.ANY &&
					predicate != EntityPredicate.ANY;
		}

		private String formatEntityTypeTag(EntityTypePredicate entityTypePred) {
			if (entityTypePred instanceof EntityTypePredicate.Tagged tagged) {
				return formatTag("entity", tagged.getTag().id());
			} else if (entityTypePred instanceof EntityTypePredicate.Single single) {
				return formatTag("entity", Registry.ENTITY_TYPE.getId(single.getType()));
			}
			return null;
		}

		private String formatLocationTag(LocationPredicate locationPred) {
			if (locationPred.getBiome() != null) {
				return formatTag("biome", locationPred.getBiome().getValue());
			} else if (locationPred.getDimension() != null) {
				return formatTag("dimension", locationPred.getDimension().getValue());
			} else if (locationPred.getFeature() != null) {
				return formatTag("structure", locationPred.getFeature().getValue());
			}
			return null;
		}

		private void addTooltips(String preposition, String tag, List<MutableText> tooltips) {
			if (tag != null) {
				tooltips.add(
						helper.writeBase().append(
								Text.translatable(preposition).formatted(Formatting.GRAY)
						).append(Text.translatable(tag))
				);
			}
		}
	}
}
