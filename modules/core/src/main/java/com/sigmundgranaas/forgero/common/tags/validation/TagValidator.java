package com.sigmundgranaas.forgero.common.tags.validation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;

import java.util.*;

/**
 * Validates tag graph integrity.
 * Detects undefined parent references, orphaned tags, and provides warnings.
 */
public class TagValidator {

	private final TagResolver resolver;

	public TagValidator(TagResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Validates the entire tag graph.
	 * @return Validation result with errors and warnings
	 */
	public ValidationResult validate() {
		List<ValidationError> errors = new ArrayList<>();
		List<ValidationWarning> warnings = new ArrayList<>();

		Set<OpenIdentifier> allTags = resolver.getAllTags();

		// Check for undefined parent references
		errors.addAll(validateParentReferences(allTags));

		// Warn about orphaned tags (no parents)
		warnings.addAll(detectOrphanedTags(allTags));

		// Cycles already detected by TagGraphBuilder

		return new ValidationResult(errors, warnings);
	}

	private List<ValidationError> validateParentReferences(Set<OpenIdentifier> allTags) {
		List<ValidationError> errors = new ArrayList<>();

		for (OpenIdentifier tag : allTags) {
			Set<OpenIdentifier> parents = resolver.getParents(tag);
			for (OpenIdentifier parent : parents) {
				if (!allTags.contains(parent)) {
					errors.add(new ValidationError(
						tag,
						"References undefined parent tag: " + parent,
						ErrorSeverity.ERROR
					));
				}
			}
		}

		return errors;
	}

	private List<ValidationWarning> detectOrphanedTags(Set<OpenIdentifier> allTags) {
		List<ValidationWarning> warnings = new ArrayList<>();

		for (OpenIdentifier tag : allTags) {
			if (resolver.getParents(tag).isEmpty()) {
				// Only warn if not a known root tag
				if (!isKnownRootTag(tag)) {
					warnings.add(new ValidationWarning(
						tag,
						"Orphaned tag with no parents (consider connecting to category root)",
						WarningSeverity.INFO
					));
				}
			}
		}

		return warnings;
	}

	private boolean isKnownRootTag(OpenIdentifier tag) {
		String path = tag.path();
		return path.equals("materials") || path.equals("parts") ||
		       path.equals("upgrades") || path.equals("tools") ||
		       path.equals("armors") || path.equals("weapons") ||
		       path.equals("schematics");
	}

	/**
	 * Validation result containing errors and warnings.
	 */
	public record ValidationResult(
		List<ValidationError> errors,
		List<ValidationWarning> warnings
	) {
		public boolean hasErrors() {
			return !errors.isEmpty();
		}

		public boolean hasWarnings() {
			return !warnings.isEmpty();
		}

		public String formatReport() {
			StringBuilder sb = new StringBuilder();
			sb.append("=== Tag Validation Report ===\n\n");

			if (!errors.isEmpty()) {
				sb.append("ERRORS (").append(errors.size()).append("):\n");
				errors.forEach(e -> sb.append("  - ").append(e.format()).append("\n"));
				sb.append("\n");
			}

			if (!warnings.isEmpty()) {
				sb.append("WARNINGS (").append(warnings.size()).append("):\n");
				warnings.forEach(w -> sb.append("  - ").append(w.format()).append("\n"));
				sb.append("\n");
			}

			if (errors.isEmpty() && warnings.isEmpty()) {
				sb.append("All tags validated successfully!\n");
			}

			return sb.toString();
		}
	}

	public record ValidationError(
		OpenIdentifier tag,
		String message,
		ErrorSeverity severity
	) {
		public String format() {
			return String.format("[%s] %s: %s", severity, tag, message);
		}
	}

	public record ValidationWarning(
		OpenIdentifier tag,
		String message,
		WarningSeverity severity
	) {
		public String format() {
			return String.format("[%s] %s: %s", severity, tag, message);
		}
	}

	public enum ErrorSeverity { ERROR, CRITICAL }
	public enum WarningSeverity { INFO, WARNING }
}
