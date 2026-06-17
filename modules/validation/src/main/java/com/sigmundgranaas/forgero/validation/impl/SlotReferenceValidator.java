package com.sigmundgranaas.forgero.validation.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.data.pipeline.api.ParsingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Validates that every {@code forgero:in_slot_type} condition references a slot classifier that an
 * upgrade, equipment, or structure slot actually provides.
 * <p>
 * A stale or mistyped {@code slot_type} is otherwise silent: the condition simply never matches, so
 * the gated attribute is dead with no parse error. (This is exactly how the secondary-material
 * {@code gem_slot}/{@code coating_slot}/… references rotted.) This validator catches that at build
 * time.
 * <p>
 * The reachability rule mirrors
 * {@link com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition}: it matches via
 * {@code TagResolver.hasTag}, so a reference {@code S} is satisfiable iff some real slot identity is
 * in {@code getDescendants(S)} — i.e. equal to {@code S}, or a descendant of {@code S} in the tag
 * graph (so {@code in_slot_type: forgero:contexts} is satisfied by a slot tagged
 * {@code forgero:contexts/offensive}).
 */
public final class SlotReferenceValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SlotReferenceValidator.class);
	private static final String IN_SLOT_TYPE = "forgero:in_slot_type";

	private SlotReferenceValidator() {
	}

	/**
	 * Scans the given content packs and returns an error for every {@code in_slot_type} condition
	 * whose {@code slot_type} no slot provides.
	 */
	public static List<ParsingError> validate(List<Path> contentPaths, TagResolver tagResolver, String defaultNamespace) {
		Set<String> classifiers = new HashSet<>();
		List<Reference> references = new ArrayList<>();

		for (Path contentPath : contentPaths) {
			Path dataDir = resolveDataDir(contentPath);
			if (dataDir == null) {
				continue;
			}
			try (Stream<Path> files = Files.walk(dataDir)) {
				files.filter(p -> p.toString().endsWith(".json"))
						.forEach(p -> scanFile(p, defaultNamespace, classifiers, references));
			} catch (IOException e) {
				LOGGER.warn("Failed to scan {} for slot references", dataDir, e);
			}
		}

		List<ParsingError> errors = new ArrayList<>();
		for (Reference ref : references) {
			if (!isReachable(ref.slotType(), classifiers, tagResolver)) {
				errors.add(new ParsingError(
						new OpenIdentifier(defaultNamespace, "in_slot_type"),
						"Unknown in_slot_type slot_type '" + ref.slotType() + "' in " + ref.source()
								+ " — no upgrade/equipment/structure slot provides this classifier, so the "
								+ "condition can never match and the gated attribute is dead. Use the slot's "
								+ "canonical tag (e.g. forgero:upgrades/types/gem).",
						null));
			}
		}
		if (!errors.isEmpty()) {
			LOGGER.error("SLOT REFERENCE ERRORS: {} in_slot_type condition(s) reference a non-existent slot type",
					errors.size());
		}
		return errors;
	}

	private static boolean isReachable(String slotType, Set<String> classifiers, TagResolver tagResolver) {
		if (classifiers.contains(slotType)) {
			return true;
		}
		OpenIdentifier id;
		try {
			id = OpenIdentifier.parse(slotType);
		} catch (RuntimeException e) {
			return false;
		}
		for (OpenIdentifier descendant : tagResolver.getDescendants(id)) {
			if (classifiers.contains(descendant.toString())) {
				return true;
			}
		}
		return false;
	}

	private static Path resolveDataDir(Path contentPath) {
		Path gradle = contentPath.resolve("src/main/resources/data");
		if (Files.isDirectory(gradle)) {
			return gradle;
		}
		Path direct = contentPath.resolve("data");
		return Files.isDirectory(direct) ? direct : null;
	}

	private static void scanFile(Path file, String ns, Set<String> classifiers, List<Reference> references) {
		try (Reader reader = Files.newBufferedReader(file)) {
			walk(JsonParser.parseReader(reader), ns, file.getFileName().toString(), classifiers, references);
		} catch (Exception e) {
			// Malformed JSON is reported by the definition parser; nothing to add here.
		}
	}

	private static void walk(JsonElement element, String ns, String source, Set<String> classifiers, List<Reference> references) {
		if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();

			if (isInSlotType(obj) && obj.has("slot_type") && obj.get("slot_type").isJsonPrimitive()) {
				references.add(new Reference(obj.get("slot_type").getAsString(), source));
			}

			// Classifier sources: upgrade slots / equipment slots (arrays) and structure slots (object).
			collectSlotArray(obj.get("upgrades"), ns, classifiers);
			JsonElement slots = obj.get("slots");
			if (slots != null && slots.isJsonArray()) {
				collectSlotArray(slots, ns, classifiers);
			} else if (slots != null && slots.isJsonObject()) {
				for (Map.Entry<String, JsonElement> entry : slots.getAsJsonObject().entrySet()) {
					collectSlotDef(entry.getValue(), ns, classifiers);
				}
			}

			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				walk(entry.getValue(), ns, source, classifiers, references);
			}
		} else if (element.isJsonArray()) {
			for (JsonElement child : element.getAsJsonArray()) {
				walk(child, ns, source, classifiers, references);
			}
		}
	}

	private static boolean isInSlotType(JsonObject obj) {
		return obj.has("type") && obj.get("type").isJsonPrimitive()
				&& IN_SLOT_TYPE.equals(obj.get("type").getAsString());
	}

	private static void collectSlotArray(JsonElement array, String ns, Set<String> classifiers) {
		if (array == null || !array.isJsonArray()) {
			return;
		}
		for (JsonElement element : array.getAsJsonArray()) {
			collectSlotDef(element, ns, classifiers);
		}
	}

	private static void collectSlotDef(JsonElement element, String ns, Set<String> classifiers) {
		if (element == null || !element.isJsonObject()) {
			return;
		}
		JsonObject slot = element.getAsJsonObject();
		addString(slot.get("type"), classifiers);
		addString(slot.get("default_tag"), classifiers);
		if (slot.has("tags") && slot.get("tags").isJsonArray()) {
			for (JsonElement tag : slot.getAsJsonArray("tags")) {
				addString(tag, classifiers);
			}
		}
		// Equipment slots carry their type as a local id (e.g. id "binding_slot" -> forgero:binding_slot).
		if (slot.has("id") && slot.get("id").isJsonPrimitive()) {
			String id = slot.get("id").getAsString();
			if (!id.contains(":")) {
				classifiers.add(ns + ":" + id);
			}
		}
	}

	private static void addString(JsonElement element, Set<String> classifiers) {
		if (element != null && element.isJsonPrimitive()) {
			classifiers.add(element.getAsString());
		}
	}

	private record Reference(String slotType, String source) {
	}
}
