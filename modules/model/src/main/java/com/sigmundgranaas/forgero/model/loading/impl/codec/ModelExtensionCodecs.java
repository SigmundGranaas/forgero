package com.sigmundgranaas.forgero.model.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.model.loading.impl.dto.LayerDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelExtensionDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.MountPointDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.SlotDTO;

import java.util.Optional;

/**
 * Codecs for parsing model extension JSON files.
 */
public class ModelExtensionCodecs {

	/**
	 * Codec for parsing {@link ModelExtensionDTO} from JSON.
	 *
	 * <p>Example JSON:</p>
	 * <pre>{@code
	 * {
	 *   "type": "forgero:model_extension",
	 *   "target": "forgero:parts/iron-pickaxe_head",
	 *   "priority": 100,
	 *   "layers": [
	 *     {
	 *       "order": 10,
	 *       "textures": {
	 *         "default": "forgero:item/overlays/dye_overlay"
	 *       }
	 *     }
	 *   ],
	 *   "slots": [
	 *     {
	 *       "id": "dye_slot",
	 *       "order": 5,
	 *       "renderer": {
	 *         "type": "forgero:component"
	 *       }
	 *     }
	 *   ],
	 *   "mount_points": [
	 *     {
	 *       "name": "charm_mount",
	 *       "position": [8, 2]
	 *     }
	 *   ]
	 * }
	 * }</pre>
	 */
	public static final Codec<ModelExtensionDTO> MODEL_EXTENSION_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("type").forGetter(ModelExtensionDTO::type),
					Codec.STRING.fieldOf("target").forGetter(ModelExtensionDTO::target),
					Codec.INT.optionalFieldOf("priority", ModelExtensionDTO.DEFAULT_PRIORITY)
							.forGetter(ModelExtensionDTO::priority),
					Codec.list(ModelCodecs.LAYER_DTO_CODEC).optionalFieldOf("layers")
							.forGetter(ModelExtensionDTO::getLayers),
					Codec.list(SlotDTO.CODEC).optionalFieldOf("slots")
							.forGetter(ModelExtensionDTO::getSlots),
					Codec.list(MountPointDTO.CODEC).optionalFieldOf("mount_points")
							.forGetter(ModelExtensionDTO::getMountPoints)
			).apply(instance, (type, target, priority, layers, slots, mountPoints) ->
					new ModelExtensionDTO(
							type,
							target,
							priority,
							layers.orElse(null),
							slots.orElse(null),
							mountPoints.orElse(null)
					)
			)
	);
}
