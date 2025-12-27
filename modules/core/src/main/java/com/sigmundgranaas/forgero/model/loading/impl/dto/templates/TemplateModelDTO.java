package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.impl.codec.JsonElementCodec;
import com.sigmundgranaas.forgero.model.loading.impl.dto.MountPointDTO;
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

	 A unified DTO for a layer within any model template.

	 It directly contains texture generation information. This can be shared by item and armor models.
	 */
	public record TemplateLayerDTO(
			int order,
			@Nullable String template,
			@Nullable String palette,
			@Nullable String output
	) {
		public static final Codec<TemplateLayerDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("order").forGetter(TemplateLayerDTO::order),
				Codec.STRING.optionalFieldOf("template").forGetter(dto -> Optional.ofNullable(dto.template)),
				Codec.STRING.optionalFieldOf("palette").forGetter(dto -> Optional.ofNullable(dto.palette)),
				Codec.STRING.optionalFieldOf("output").forGetter(dto -> Optional.ofNullable(dto.output))
		).apply(instance, (order, template, palette, output) -> new TemplateLayerDTO(order, template.orElse(null), palette.orElse(null), output.orElse(null))));
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
