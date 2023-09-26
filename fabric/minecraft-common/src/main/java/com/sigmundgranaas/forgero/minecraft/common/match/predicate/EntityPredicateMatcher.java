package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriterBuilder;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.WriterHelper;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY_TARGET;

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

		@Override
		public List<MutableText> write(Matchable matchable) {
			MutableText against = helper.writeBase().append(Text.translatable("tooltip.forgero.against").formatted(Formatting.GRAY));

			return List.of(against);
		}
	}
}
