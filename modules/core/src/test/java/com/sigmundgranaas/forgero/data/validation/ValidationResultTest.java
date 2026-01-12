package com.sigmundgranaas.forgero.data.validation;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Result Behavior")
class ValidationResultTest {

	private static final IdentifierFactory ID_FACTORY = new IdentifierFactory.Builder()
			.defaultNamespace("forgero")
			.build();

	private static OpenIdentifier id(String path) {
		return ID_FACTORY.of(path);
	}

	@Nested
	@DisplayName("Empty Result")
	class EmptyResultBehavior {

		@Test
		@DisplayName("empty result has no errors")
		void emptyResultHasNoErrors() {
			ValidationResult result = ValidationResult.empty();

			assertFalse(result.hasErrors());
			assertFalse(result.hasWarnings());
			assertFalse(result.hasIssues());
			assertTrue(result.issues().isEmpty());
		}

		@Test
		@DisplayName("empty result error count is zero")
		void emptyResultErrorCountIsZero() {
			ValidationResult result = ValidationResult.empty();

			assertEquals(0, result.errorCount());
			assertEquals(0, result.warningCount());
		}
	}

	@Nested
	@DisplayName("Result with Issues")
	class ResultWithIssuesBehavior {

		@Test
		@DisplayName("result with errors reports hasErrors true")
		void resultWithErrorsReportsHasErrorsTrue() {
			ValidationResult result = ValidationResult.of(List.of(
					ValidationIssue.error(id("test"), "Test error")
			));

			assertTrue(result.hasErrors());
			assertTrue(result.hasIssues());
			assertEquals(1, result.errorCount());
		}

		@Test
		@DisplayName("result with warnings reports hasWarnings true")
		void resultWithWarningsReportsHasWarningsTrue() {
			ValidationResult result = ValidationResult.of(List.of(
					ValidationIssue.warning(id("test"), "Test warning")
			));

			assertTrue(result.hasWarnings());
			assertTrue(result.hasIssues());
			assertFalse(result.hasErrors());
			assertEquals(1, result.warningCount());
		}

		@Test
		@DisplayName("errors method filters only errors")
		void errorsMethodFiltersOnlyErrors() {
			ValidationResult result = ValidationResult.of(List.of(
					ValidationIssue.error(id("test1"), "Error 1"),
					ValidationIssue.warning(id("test2"), "Warning 1"),
					ValidationIssue.error(id("test3"), "Error 2")
			));

			List<ValidationIssue> errors = result.errors();
			assertEquals(2, errors.size());
			assertTrue(errors.stream().allMatch(e -> e.severity() == ValidationSeverity.ERROR));
		}

		@Test
		@DisplayName("warnings method filters only warnings")
		void warningsMethodFiltersOnlyWarnings() {
			ValidationResult result = ValidationResult.of(List.of(
					ValidationIssue.error(id("test1"), "Error 1"),
					ValidationIssue.warning(id("test2"), "Warning 1"),
					ValidationIssue.warning(id("test3"), "Warning 2")
			));

			List<ValidationIssue> warnings = result.warnings();
			assertEquals(2, warnings.size());
			assertTrue(warnings.stream().allMatch(e -> e.severity() == ValidationSeverity.WARNING));
		}
	}

	@Nested
	@DisplayName("Result Merging")
	class ResultMergingBehavior {

		@Test
		@DisplayName("merge combines issues from multiple results")
		void mergeCombinesIssuesFromMultipleResults() {
			ValidationResult result1 = ValidationResult.of(List.of(
					ValidationIssue.error(id("test1"), "Error 1")
			));
			ValidationResult result2 = ValidationResult.of(List.of(
					ValidationIssue.warning(id("test2"), "Warning 1")
			));

			ValidationResult merged = ValidationResult.merge(List.of(result1, result2));

			assertEquals(2, merged.issues().size());
			assertEquals(1, merged.errorCount());
			assertEquals(1, merged.warningCount());
		}

		@Test
		@DisplayName("builder accumulates issues correctly")
		void builderAccumulatesIssuesCorrectly() {
			ValidationResult result = new ValidationResult.Builder()
					.add(ValidationIssue.error(id("test1"), "Error 1"))
					.add(ValidationIssue.warning(id("test2"), "Warning 1"))
					.add(ValidationIssue.error(id("test3"), "Error 2"))
					.build();

			assertEquals(3, result.issues().size());
			assertEquals(2, result.errorCount());
			assertEquals(1, result.warningCount());
		}
	}

	@Nested
	@DisplayName("Report Formatting")
	class ReportFormattingBehavior {

		@Test
		@DisplayName("report includes error count header")
		void reportIncludesErrorCountHeader() {
			ValidationResult result = ValidationResult.of(List.of(
					ValidationIssue.error(id("test1"), "Error 1"),
					ValidationIssue.error(id("test2"), "Error 2")
			));

			String report = result.formatReport();

			assertTrue(report.contains("ERRORS (2)"));
		}

		@Test
		@DisplayName("report includes warning count header")
		void reportIncludesWarningCountHeader() {
			ValidationResult result = ValidationResult.of(List.of(
					ValidationIssue.warning(id("test1"), "Warning 1"),
					ValidationIssue.warning(id("test2"), "Warning 2"),
					ValidationIssue.warning(id("test3"), "Warning 3")
			));

			String report = result.formatReport();

			assertTrue(report.contains("WARNINGS (3)"));
		}

		@Test
		@DisplayName("report shows success when no issues")
		void reportShowsSuccessWhenNoIssues() {
			ValidationResult result = ValidationResult.empty();

			String report = result.formatReport();

			assertTrue(report.contains("validated successfully"));
		}

		@Test
		@DisplayName("report includes summary")
		void reportIncludesSummary() {
			ValidationResult result = ValidationResult.of(List.of(
					ValidationIssue.error(id("test1"), "Error 1"),
					ValidationIssue.warning(id("test2"), "Warning 1")
			));

			String report = result.formatReport();

			assertTrue(report.contains("Summary:"));
			assertTrue(report.contains("1 error(s)"));
			assertTrue(report.contains("1 warning(s)"));
		}
	}
}
