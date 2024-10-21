package com.sigmundgranaas.forgero.minecraft.common.client.predicate;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.client.render.model.json.ModelTransformationMode;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.client.model.baked.DefaultedDynamicBakedModel.RENDER_MODE;

public record RenderModePredicate(ModelTransformationMode mode) implements Matchable {
	public static String ID = "forgero:render_mode";

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		Optional<ModelTransformationMode> mode = context.get(RENDER_MODE);
		return mode.filter(modelTransformationMode -> modelTransformationMode == this.mode()).isPresent();
	}

	/**
	 * Attempts to create a Matchable of type DamagePercentagePredicate from a JsonElement if the element is identified as a "DamagePercentagePredicate".
	 */
	public static class RenderModePredicatePredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID).map(jsonObject -> new RenderModePredicate(ModelTransformationMode.valueOf(jsonObject.get("render_mode").getAsString())));
		}
	}
}
