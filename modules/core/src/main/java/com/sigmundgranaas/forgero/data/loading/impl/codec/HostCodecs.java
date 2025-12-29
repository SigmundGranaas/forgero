package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.CreateTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.IdentifierTemplateEntry;

import java.util.Optional;

public class HostCodecs {

	public static final Codec<IdentifierEntry> IDENTIFIER_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("type").forGetter(IdentifierEntry::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(IdentifierEntry::id)
			).apply(instance, IdentifierEntry::new));

	public static final Codec<CreateData> CREATE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id").forGetter(CreateData::id),
					Codec.STRING.fieldOf("class_name").forGetter(CreateData::itemClass),
					Codec.STRING.optionalFieldOf("item_group").forGetter(data -> Optional.ofNullable(data.itemGroup()))
			).apply(instance, (id, className, itemGroup) -> new CreateData(id, className, itemGroup.orElse(null))));

	public static final Codec<HostData> HOST_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.list(IDENTIFIER_ENTRY_CODEC).optionalFieldOf("identifiers").forGetter(data -> Optional.ofNullable(data.identifiers())),
					CREATE_DATA_CODEC.optionalFieldOf("create").forGetter(data -> Optional.ofNullable(data.create()))
			).apply(instance, (identifiers, create) -> new HostData(identifiers.orElse(null), create.orElse(null))));


	public static final Codec<IdentifierTemplateEntry> IDENTIFIER_TEMPLATE_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("type").forGetter(IdentifierTemplateEntry::type),
					Codec.STRING.fieldOf("id").forGetter(IdentifierTemplateEntry::id)
			).apply(instance, IdentifierTemplateEntry::new));

	public static final Codec<CreateTemplateData> CREATE_TEMPLATE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("id").forGetter(CreateTemplateData::id),
					Codec.STRING.fieldOf("class_name").forGetter(CreateTemplateData::className),
					Codec.STRING.optionalFieldOf("item_group").forGetter(data -> Optional.ofNullable(data.itemGroup()))
			).apply(instance, (id, className, itemGroup) -> new CreateTemplateData(id, className, itemGroup.orElse(null))));

	/**
	 * Flat codec for host_template that reads "class" and "id" at the top level.
	 * This matches the ergonomic JSON format:
	 * <pre>
	 * "host_template": {
	 *   "class": "forgero:pickaxe_item",
	 *   "id": "forgero:{head.material.name}-pickaxe",
	 *   "item_group": "minecraft:tools_and_utilities"  // optional
	 * }
	 * </pre>
	 */
	public static final Codec<HostTemplateData> HOST_TEMPLATE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.list(IDENTIFIER_TEMPLATE_ENTRY_CODEC).optionalFieldOf("identifiers").forGetter(data -> Optional.ofNullable(data.identifiers())),
					Codec.STRING.optionalFieldOf("class").forGetter(data -> Optional.ofNullable(data.create()).map(CreateTemplateData::className)),
					Codec.STRING.optionalFieldOf("id").forGetter(data -> Optional.ofNullable(data.create()).map(CreateTemplateData::id)),
					Codec.STRING.optionalFieldOf("item_group").forGetter(data -> Optional.ofNullable(data.create()).flatMap(c -> Optional.ofNullable(c.itemGroup())))
			).apply(instance, (identifiers, className, id, itemGroup) -> {
				CreateTemplateData create = null;
				if (className.isPresent() && id.isPresent()) {
					create = new CreateTemplateData(id.get(), className.get(), itemGroup.orElse(null));
				}
				return new HostTemplateData(identifiers.orElse(null), create);
			}));
}
