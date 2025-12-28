package com.sigmundgranaas.forgero.drp.impl.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * In-memory implementation of Minecraft's ResourcePack interface.
 */
public class InMemoryResourcePack implements ResourcePack {

	private static final Logger LOGGER = LoggerFactory.getLogger("DRP-Pack");
	private static final int PACK_FORMAT = 15; // 1.20.1 pack format

	private final String name;
	private final DynamicResourcePackImpl pack;

	public InMemoryResourcePack(DynamicResourcePackImpl pack) {
		this.pack = pack;
		this.name = pack.getId().toString();
		LOGGER.debug("Created InMemoryResourcePack: {}", name);
		LOGGER.debug("  Data IDs: {}", pack.getRegistry().getDataIds());
		LOGGER.debug("  Data namespaces: {}", pack.getRegistry().getDataNamespaces());
	}

	@Override
	public InputSupplier<InputStream> openRoot(String... segments) {
		String path = String.join("/", segments);

		// Handle pack.mcmeta
		if (path.equals("pack.mcmeta")) {
			String mcmeta = """
					{
					  "pack": {
					    "pack_format": 15,
					    "description": "%s"
					  }
					}
					""".formatted(pack.getDescription() != null ? pack.getDescription() : "Dynamic Resource Pack");
			byte[] data = mcmeta.getBytes(StandardCharsets.UTF_8);
			return () -> new ByteArrayInputStream(data);
		}

		return null;
	}

	@Override
	public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
		byte[] data;
		if (type == ResourceType.SERVER_DATA) {
			data = pack.getRegistry().getData(id);
		} else {
			data = pack.getRegistry().getAsset(id);
		}

		if (data != null) {
			return () -> new ByteArrayInputStream(data);
		}
		return null;
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
		Set<Identifier> ids;
		if (type == ResourceType.SERVER_DATA) {
			ids = pack.getRegistry().getDataIds();
		} else {
			ids = pack.getRegistry().getAssetIds();
		}

		for (Identifier id : ids) {
			if (id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix)) {
				InputSupplier<InputStream> supplier = open(type, id);
				if (supplier != null) {
					consumer.accept(id, supplier);
				}
			}
		}
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		if (type == ResourceType.SERVER_DATA) {
			return pack.getRegistry().getDataNamespaces();
		} else {
			return pack.getRegistry().getAssetNamespaces();
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
		// Handle pack metadata request
		if (metaReader.getKey().equals("pack")) {
			String description = pack.getDescription() != null ? pack.getDescription() : "Dynamic Resource Pack";
			return (T) new PackResourceMetadata(Text.literal(description), PACK_FORMAT);
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void close() {
		// Nothing to close for in-memory pack
	}
}
