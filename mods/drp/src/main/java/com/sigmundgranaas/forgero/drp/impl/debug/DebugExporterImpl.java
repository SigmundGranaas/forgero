package com.sigmundgranaas.forgero.drp.impl.debug;

import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.debug.DebugExporter;
import com.sigmundgranaas.forgero.drp.impl.pack.DynamicResourcePackImpl;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of DebugExporter for writing resource packs to disk.
 */
public class DebugExporterImpl implements DebugExporter {

	private final Path outputPath;
	private final List<DynamicResourcePack> packs;
	private Set<String> includedTypes = null;
	private boolean prettyPrint = true;
	private String namespaceFilter = null;

	public DebugExporterImpl(Path outputPath, List<DynamicResourcePack> packs) {
		this.outputPath = outputPath;
		this.packs = packs;
	}

	@Override
	public DebugExporter includeTypes(Set<String> types) {
		this.includedTypes = new HashSet<>(types);
		return this;
	}

	@Override
	public DebugExporter includeAll() {
		this.includedTypes = null;
		return this;
	}

	@Override
	public DebugExporter prettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
		return this;
	}

	@Override
	public DebugExporter filterNamespace(String namespace) {
		this.namespaceFilter = namespace;
		return this;
	}

	@Override
	public int export() {
		return exportWithSummary().totalExported();
	}

	@Override
	public ExportSummary exportWithSummary() {
		int tags = 0;
		int recipes = 0;
		int models = 0;
		int lang = 0;
		int textures = 0;

		try {
			Files.createDirectories(outputPath);

			for (DynamicResourcePack pack : packs) {
				if (!(pack instanceof DynamicResourcePackImpl impl)) {
					continue;
				}

				// Export data resources
				for (Identifier id : impl.getRegistry().getDataIds()) {
					if (!matchesFilters(id)) continue;

					String type = getResourceType(id.getPath());
					if (includedTypes != null && !includedTypes.contains(type)) continue;

					Path filePath = outputPath.resolve("data").resolve(id.getNamespace()).resolve(id.getPath());
					writeFile(filePath, impl.getRegistry().getData(id));

					if (type.equals("tags")) tags++;
					else if (type.equals("recipes")) recipes++;
				}

				// Export asset resources
				for (Identifier id : impl.getRegistry().getAssetIds()) {
					if (!matchesFilters(id)) continue;

					String type = getResourceType(id.getPath());
					if (includedTypes != null && !includedTypes.contains(type)) continue;

					Path filePath = outputPath.resolve("assets").resolve(id.getNamespace()).resolve(id.getPath());
					writeFile(filePath, impl.getRegistry().getAsset(id));

					if (type.equals("models")) models++;
					else if (type.equals("lang")) lang++;
					else if (type.equals("textures")) textures++;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to export resources", e);
		}

		int total = tags + recipes + models + lang + textures;
		return new ExportSummaryImpl(total, tags, recipes, models, lang, textures, outputPath);
	}

	private boolean matchesFilters(Identifier id) {
		if (namespaceFilter != null && !id.getNamespace().equals(namespaceFilter)) {
			return false;
		}
		return true;
	}

	private String getResourceType(String path) {
		if (path.startsWith("tags/")) return "tags";
		if (path.startsWith("recipes/")) return "recipes";
		if (path.startsWith("models/")) return "models";
		if (path.startsWith("lang/")) return "lang";
		if (path.startsWith("textures/")) return "textures";
		if (path.startsWith("atlases/")) return "atlases";
		return "other";
	}

	private void writeFile(Path path, byte[] data) throws IOException {
		Files.createDirectories(path.getParent());
		Files.write(path, data);
	}

	/**
	 * Implementation of ExportSummary.
	 */
	private record ExportSummaryImpl(
			int totalExported,
			int tagsExported,
			int recipesExported,
			int modelsExported,
			int langExported,
			int texturesExported,
			Path outputPath
	) implements ExportSummary {
	}
}
