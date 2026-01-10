package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.impl.codec.JsonElementCodec;
import com.sigmundgranaas.forgero.model.loading.impl.dto.MountPointDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.PredicateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.SlotDTO;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**

 DTO for the "model" block inside an item template file.

 This DTO now consistently uses a 'layers' list for all textures, unifying its structure.

 A "simple texture model" is now just a model with a single layer.
 */
public record TemplateModelDTO(
		@Nullable String id,
		String type,
		@Nullable List<TemplateLayerDTO> layers,
		@Nullable List<SlotDTO> slots,
		@Nullable List<MountPointDTO> mountPoints,
		@Nullable String target,
		@Nullable String context,
		@Nullable String parent,
		@Nullable JsonElement display
) {

	/**
	 * A unified DTO for a layer within any model template.
	 *
	 * It directly contains texture generation information. This can be shared by item and armor models.
	 * 
	 * <p>Supports two modes:
	 * <ol>
	 *   <li><b>Texture Generation Mode</b>: Uses {@code template}, {@code palette}, and {@code output}
	 *       to generate textures at load time from a grayscale template and color palette.</li>
	 *   <li><b>Runtime Variant Mode</b>: Uses {@code textures.variants} to select different textures
	 *       at runtime based on predicates (e.g., root_tag to change texture based on tool type).</li>
	 * </ol>
	 * 
	 * <p>Both modes can be combined: the generated texture becomes the default, while variants
	 * provide runtime texture switching based on context.
	 * 
	 * @param order The rendering order (lower values render first/behind)
	 * @param template Path to the grayscale texture template for generation
	 * @param palette Path to the color palette for generation
	 * @param output Output path for the generated texture
	 * @param textures Optional runtime textures configuration with default and variants
	 */
	public record TemplateLayerDTO(
			int order,
			@Nullable String template,
			@Nullable String palette,
			@Nullable String output,
			@Nullable TemplateTexturesDTO textures
	) {
		public static final Codec<TemplateLayerDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("order").forGetter(TemplateLayerDTO::order),
				Codec.STRING.optionalFieldOf("template").forGetter(dto -> Optional.ofNullable(dto.template)),
				Codec.STRING.optionalFieldOf("palette").forGetter(dto -> Optional.ofNullable(dto.palette)),
				Codec.STRING.optionalFieldOf("output").forGetter(dto -> Optional.ofNullable(dto.output)),
				TemplateTexturesDTO.CODEC.optionalFieldOf("textures").forGetter(dto -> Optional.ofNullable(dto.textures))
		).apply(instance, (order, template, palette, output, textures) -> 
				new TemplateLayerDTO(order, template.orElse(null), palette.orElse(null), output.orElse(null), textures.orElse(null))));
	}

	/**
	 * DTO for the textures block within a template layer.
	 * Supports a default texture path (with placeholders) and optional variants.
	 * 
	 * <p>Placeholders like {@code {target.name}} are resolved during model generation
	 * to produce material-specific texture paths.
	 * 
	 * @param defaultTexture The default texture path (may contain placeholders)
	 * @param variants Optional list of conditional texture variants
	 */
	public record TemplateTexturesDTO(
			@Nullable String defaultTexture,
			@Nullable List<TemplateVariantDTO> variants
	) {
		public static final Codec<TemplateTexturesDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("default").forGetter(dto -> Optional.ofNullable(dto.defaultTexture)),
				Codec.list(TemplateVariantDTO.CODEC).optionalFieldOf("variants").forGetter(dto -> Optional.ofNullable(dto.variants))
		).apply(instance, (def, variants) -> new TemplateTexturesDTO(def.orElse(null), variants.orElse(null))));
	}

	/**
	 * DTO for a texture variant within a template.
	 * Supports two modes:
	 * <ol>
	 *   <li><b>Direct texture</b>: Uses {@code texture} to specify a pre-existing texture path</li>
	 *   <li><b>Generated texture</b>: Uses {@code template}, {@code palette}, and {@code output}
	 *       to generate the variant texture from a grayscale template and color palette</li>
	 * </ol>
	 * 
	 * @param predicate List of predicates that must all match for this variant to be active
	 * @param texture The texture path (may contain placeholders)
	 * @param template Path to the grayscale texture template for generation
	 * @param palette Path to the color palette for generation
	 * @param output Output path for the generated texture
	 */
	public record TemplateVariantDTO(
			List<PredicateDTO> predicate,
			@Nullable String texture,
			@Nullable String template,
			@Nullable String palette,
			@Nullable String output
	) {
		public static final Codec<TemplateVariantDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.list(PredicateDTO.CODEC).fieldOf("predicate").forGetter(TemplateVariantDTO::predicate),
				Codec.STRING.optionalFieldOf("texture").forGetter(dto -> Optional.ofNullable(dto.texture)),
				Codec.STRING.optionalFieldOf("template").forGetter(dto -> Optional.ofNullable(dto.template)),
				Codec.STRING.optionalFieldOf("palette").forGetter(dto -> Optional.ofNullable(dto.palette)),
				Codec.STRING.optionalFieldOf("output").forGetter(dto -> Optional.ofNullable(dto.output))
		).apply(instance, (predicate, texture, template, palette, output) -> 
				new TemplateVariantDTO(predicate, texture.orElse(null), template.orElse(null), palette.orElse(null), output.orElse(null))));
	}

	public static final Codec<TemplateModelDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("id").forGetter(dto -> Optional.ofNullable(dto.id)),
			Codec.STRING.fieldOf("type").forGetter(TemplateModelDTO::type),
			Codec.list(TemplateLayerDTO.CODEC).optionalFieldOf("layers").forGetter(dto -> Optional.ofNullable(dto.layers)),
			Codec.list(SlotDTO.CODEC).optionalFieldOf("slots").forGetter(dto -> Optional.ofNullable(dto.slots)),
			Codec.list(MountPointDTO.CODEC).optionalFieldOf("mount_points").forGetter(dto -> Optional.ofNullable(dto.mountPoints)),
			Codec.STRING.optionalFieldOf("target").forGetter(dto -> Optional.ofNullable(dto.target)),
			Codec.STRING.optionalFieldOf("context").forGetter(dto -> Optional.ofNullable(dto.context)),
			Codec.STRING.optionalFieldOf("parent").forGetter(dto -> Optional.ofNullable(dto.parent)),
			JsonElementCodec.INSTANCE.optionalFieldOf("display").forGetter(dto -> Optional.ofNullable(dto.display))
	).apply(instance, (id, type, layers, slots, mountPoints, target, context, parent, display) -> new TemplateModelDTO(id.orElse(null), type, layers.orElse(null), slots.orElse(null), mountPoints.orElse(null), target.orElse(null), context.orElse(null), parent.orElse(null), display.orElse(null))));
}
