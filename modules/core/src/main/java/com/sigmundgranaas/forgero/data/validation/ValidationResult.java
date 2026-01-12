package com.sigmundgranaas.forgero.data.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates validation issues from multiple validators.
 * Immutable after construction.
 */
public final class ValidationResult {
	private final List<ValidationIssue> issues;

	private ValidationResult(List<ValidationIssue> issues) {
		this.issues = Collections.unmodifiableList(new ArrayList<>(issues));
	}

	/**
	 * Creates an empty (successful) validation result.
	 */
	public static ValidationResult empty() {
		return new ValidationResult(List.of());
	}

	/**
	 * Creates a validation result with the given issues.
	 */
	public static ValidationResult of(List<ValidationIssue> issues) {
		return new ValidationResult(issues);
	}

	/**
	 * Merges multiple validation results into one.
	 */
	public static ValidationResult merge(List<ValidationResult> results) {
		List<ValidationIssue> allIssues = results.stream()
				.flatMap(r -> r.issues.stream())
				.collect(Collectors.toList());
		return new ValidationResult(allIssues);
	}

	/**
	 * @return true if there are any ERROR-level issues.
	 */
	public boolean hasErrors() {
		return issues.stream().anyMatch(i -> i.severity() == ValidationSeverity.ERROR);
	}

	/**
	 * @return true if there are any WARNING-level issues.
	 */
	public boolean hasWarnings() {
		return issues.stream().anyMatch(i -> i.severity() == ValidationSeverity.WARNING);
	}

	/**
	 * @return true if there are any issues (any severity).
	 */
	public boolean hasIssues() {
		return !issues.isEmpty();
	}

	/**
	 * @return All issues.
	 */
	public List<ValidationIssue> issues() {
		return issues;
	}

	/**
	 * @return Only ERROR-level issues.
	 */
	public List<ValidationIssue> errors() {
		return issues.stream()
				.filter(i -> i.severity() == ValidationSeverity.ERROR)
				.collect(Collectors.toList());
	}

	/**
	 * @return Only WARNING-level issues.
	 */
	public List<ValidationIssue> warnings() {
		return issues.stream()
				.filter(i -> i.severity() == ValidationSeverity.WARNING)
				.collect(Collectors.toList());
	}

	/**
	 * @return Count of errors.
	 */
	public int errorCount() {
		return (int) issues.stream().filter(i -> i.severity() == ValidationSeverity.ERROR).count();
	}

	/**
	 * @return Count of warnings.
	 */
	public int warningCount() {
		return (int) issues.stream().filter(i -> i.severity() == ValidationSeverity.WARNING).count();
	}

	/**
	 * Formats a human-readable report of all validation issues.
	 */
	public String formatReport() {
		StringBuilder sb = new StringBuilder();
		sb.append("=== Component Validation Report ===\n\n");

		List<ValidationIssue> errors = errors();
		List<ValidationIssue> warnings = warnings();

		if (!errors.isEmpty()) {
			sb.append("ERRORS (").append(errors.size()).append("):\n");
			errors.forEach(e -> sb.append("  ").append(e.format()).append("\n\n"));
		}

		if (!warnings.isEmpty()) {
			sb.append("WARNINGS (").append(warnings.size()).append("):\n");
			warnings.forEach(w -> sb.append("  ").append(w.format()).append("\n\n"));
		}

		if (errors.isEmpty() && warnings.isEmpty()) {
			sb.append("All components validated successfully!\n");
		} else {
			sb.append("Summary: ")
					.append(errors.size()).append(" error(s), ")
					.append(warnings.size()).append(" warning(s)\n");
		}

		return sb.toString();
	}

	/**
	 * Formats only errors for concise logging.
	 */
	public String formatErrors() {
		if (!hasErrors()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Validation Errors (").append(errorCount()).append("):\n");
		errors().forEach(e -> sb.append("  ").append(e.format()).append("\n"));
		return sb.toString();
	}

	/**
	 * Formats only warnings for concise logging.
	 */
	public String formatWarnings() {
		if (!hasWarnings()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Validation Warnings (").append(warningCount()).append("):\n");
		warnings().forEach(w -> sb.append("  ").append(w.format()).append("\n"));
		return sb.toString();
	}

	/**
	 * Builder for accumulating validation issues.
	 */
	public static class Builder {
		private final List<ValidationIssue> issues = new ArrayList<>();

		public Builder add(ValidationIssue issue) {
			issues.add(issue);
			return this;
		}

		public Builder addAll(List<ValidationIssue> issues) {
			this.issues.addAll(issues);
			return this;
		}

		public Builder merge(ValidationResult result) {
			this.issues.addAll(result.issues());
			return this;
		}

		public ValidationResult build() {
			return new ValidationResult(issues);
		}
	}
}
