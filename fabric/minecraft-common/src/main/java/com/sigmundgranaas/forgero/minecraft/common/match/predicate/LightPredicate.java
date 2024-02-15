package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LightPredicate {

	public static final LightPredicate ANY = new LightPredicate(NumberRange.IntRange.ANY);

	private NumberRange.IntRange range;

	public static LightPredicate fromJson(@Nullable JsonElement json) {
		if (json != null && !json.isJsonNull()) {
			JsonObject jsonObject = JsonHelper.asObject(json, "light");
			NumberRange.IntRange intRange = NumberRange.IntRange.fromJson(jsonObject.get("light"));
			return new LightPredicate(intRange);
		} else {
			return ANY;
		}
	}

	public boolean test(World world, BlockPos pos) {
		if (this == ANY) {
			return true;
		} else if (!world.canSetBlock(pos)) {
			return false;
		} else {
			return this.range.test(world.getLightLevel(pos));
		}
	}
}
