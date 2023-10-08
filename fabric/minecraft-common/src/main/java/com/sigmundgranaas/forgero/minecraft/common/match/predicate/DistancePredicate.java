package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import net.minecraft.predicate.NumberRange;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistancePredicate {
	public static final DistancePredicate ANY = new DistancePredicate(
			NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY,
			NumberRange.FloatRange.ANY, NumberRange.FloatRange.ANY,
			NumberRange.FloatRange.ANY);

	private NumberRange.FloatRange x;
	private NumberRange.FloatRange y;
	private NumberRange.FloatRange z;
	private NumberRange.FloatRange horizontal;
	private NumberRange.FloatRange absolute;


	public static DistancePredicate y(NumberRange.FloatRange y) {
		return new DistancePredicate(NumberRange.FloatRange.ANY,
				y,
				NumberRange.FloatRange.ANY,
				NumberRange.FloatRange.ANY,
				NumberRange.FloatRange.ANY);
	}

	public static DistancePredicate fromJson(@Nullable JsonElement json) {
		if (json != null && !json.isJsonNull()) {
			JsonObject jsonObject = JsonHelper.asObject(json, "distance");
			return new DistancePredicate(
					NumberRange.FloatRange.fromJson(jsonObject.get("x")),
					NumberRange.FloatRange.fromJson(jsonObject.get("y")),
					NumberRange.FloatRange.fromJson(jsonObject.get("z")),
					NumberRange.FloatRange.fromJson(jsonObject.get("horizontal")),
					NumberRange.FloatRange.fromJson(jsonObject.get("absolute"))
			);
		}
		return ANY;
	}

	public boolean test(double x0, double y0, double z0, double x1, double y1, double z1) {
		float f = (float) (x0 - x1);
		float g = (float) (y0 - y1);
		float h = (float) (z0 - z1);

		if (this.x.test(MathHelper.abs(f)) && this.y.test(MathHelper.abs(g)) && this.z.test(MathHelper.abs(h))) {
			if (!this.horizontal.testSqrt(f * f + h * h)) {
				return false;
			} else {
				return this.absolute.testSqrt(f * f + g * g + h * h);
			}
		} else {
			return false;
		}
	}
}
