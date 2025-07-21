package com.sigmundgranaas.forgero.armor.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class DisplayCodecs {
	private static DataResult<Vector3f> toVector3f(List<Float> list) {
		if(list.size() == 1){
			return DataResult.success(new Vector3f(list.get(0), list.get(0), list.get(0)));
		}
		if (list.size() != 3) {
			return DataResult.error(() -> "Expected 3 floats for a vector, got " + list.size());
		}
		return DataResult.success(new Vector3f(list.get(0), list.get(1), list.get(2)));
	}

	private static final Codec<Vector3f> VECTOR_3F_CODEC = Codec.FLOAT.listOf().comapFlatMap(DisplayCodecs::toVector3f, v -> List.of(v.x(), v.y(), v.z()));

	public static final Codec<Transformation> TRANSFORMATION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			VECTOR_3F_CODEC.optionalFieldOf("rotation", new Vector3f(0.0F, 0.0F, 0.0F)).forGetter(t -> t.rotation),
			VECTOR_3F_CODEC.optionalFieldOf("translation", new Vector3f(0.0F, 0.0F, 0.0F)).forGetter(t -> new Vector3f(t.translation).mul(16.0F)),
			VECTOR_3F_CODEC.optionalFieldOf("scale", new Vector3f(1.0F, 1.0F, 1.0F)).forGetter(t -> t.scale)
	).apply(instance, (rotation, translation, scale) -> {

		Vector3f clampedTranslation = new Vector3f(
				MathHelper.clamp(translation.x(), -80.0F, 80.0F),
				MathHelper.clamp(translation.y(), -80.0F, 80.0F),
				MathHelper.clamp(translation.z(), -80.0F, 80.0F)
		);

		Vector3f clampedScale = new Vector3f(
				MathHelper.clamp(scale.x(), -4.0F, 4.0F),
				MathHelper.clamp(scale.y(), -4.0F, 4.0F),
				MathHelper.clamp(scale.z(), -4.0F, 4.0F)
		);

		// Vanilla deserializer multiplies by 0.0625f (1/16)
		return new Transformation(rotation, clampedTranslation.mul(0.0625F), clampedScale);
	}));

	private static Optional<Transformation> getOptionalTransform(ModelTransformation mt, ModelTransformationMode mode) {
		return mt.isTransformationDefined(mode) ? Optional.of(mt.getTransformation(mode)) : Optional.empty();
	}

	public static final Codec<ModelTransformation> MODEL_TRANSFORMATION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TRANSFORMATION_CODEC.optionalFieldOf("thirdperson_righthand").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND)),
			TRANSFORMATION_CODEC.optionalFieldOf("thirdperson_lefthand").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.THIRD_PERSON_LEFT_HAND)),
			TRANSFORMATION_CODEC.optionalFieldOf("firstperson_righthand").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND)),
			TRANSFORMATION_CODEC.optionalFieldOf("firstperson_lefthand").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.FIRST_PERSON_LEFT_HAND)),
			TRANSFORMATION_CODEC.optionalFieldOf("head").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.HEAD)),
			TRANSFORMATION_CODEC.optionalFieldOf("gui").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.GUI)),
			TRANSFORMATION_CODEC.optionalFieldOf("ground").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.GROUND)),
			TRANSFORMATION_CODEC.optionalFieldOf("fixed").forGetter(mt -> getOptionalTransform(mt, ModelTransformationMode.FIXED))
	).apply(instance, (tpr, tpl, fpr, fpl, head, gui, ground, fixed) -> {

		Transformation thirdRight = tpr.orElse(Transformation.IDENTITY);
		Transformation thirdLeft = tpl.orElse(thirdRight);

		Transformation firstRight = fpr.orElse(Transformation.IDENTITY);
		Transformation firstLeft = fpl.orElse(firstRight);

		// The constructor order matters: (third_left, third_right, first_left, first_right, head, gui, ground, fixed)
		return new ModelTransformation(
				thirdLeft,
				thirdRight,
				firstLeft,
				firstRight,
				head.orElse(Transformation.IDENTITY),
				gui.orElse(Transformation.IDENTITY),
				ground.orElse(Transformation.IDENTITY),
				fixed.orElse(Transformation.IDENTITY)
		);
	}));
}
