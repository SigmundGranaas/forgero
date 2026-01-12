package com.sigmundgranaas.forgero.data.validation;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Issue Behavior")
class ValidationIssueTest {

	private static final IdentifierFactory ID_FACTORY = new IdentifierFactory.Builder()
			.defaultNamespace("forgero")
			.build();

	private static OpenIdentifier id(String path) {
		return ID_FACTORY.of(path);
	}

	@Nested
	@DisplayName("Error Factory Methods")
	class ErrorFactoryMethods {

		@Test
		@DisplayName("error creates ERROR severity issue")
		void errorCreatesErrorSeverityIssue() {
			ValidationIssue issue = ValidationIssue.error(id("test"), "Test message");

			assertEquals(ValidationSeverity.ERROR, issue.severity());
			assertEquals(id("test"), issue.componentId());
			assertEquals("Test message", issue.message());
			assertTrue(issue.location().isEmpty());
			assertTrue(issue.suggestion().isEmpty());
		}

		@Test
		@DisplayName("error with location includes location")
		void errorWithLocationIncludesLocation() {
			ValidationIssue issue = ValidationIssue.error(id("test"), "Test message", "properties.attr[0]");

			assertTrue(issue.location().isPresent());
			assertEquals("properties.attr[0]", issue.location().get());
		}

		@Test
		@DisplayName("error with suggestion includes suggestion")
		void errorWithSuggestionIncludesSuggestion() {
			ValidationIssue issue = ValidationIssue.error(
					id("test"), "Test message", "properties.attr[0]", "Add a multiplier");

			assertTrue(issue.suggestion().isPresent());
			assertEquals("Add a multiplier", issue.suggestion().get());
		}
	}

	@Nested
	@DisplayName("Warning Factory Methods")
	class WarningFactoryMethods {

		@Test
		@DisplayName("warning creates WARNING severity issue")
		void warningCreatesWarningSeverityIssue() {
			ValidationIssue issue = ValidationIssue.warning(id("test"), "Test warning");

			assertEquals(ValidationSeverity.WARNING, issue.severity());
		}
	}

	@Nested
	@DisplayName("Info Factory Methods")
	class InfoFactoryMethods {

		@Test
		@DisplayName("info creates INFO severity issue")
		void infoCreatesInfoSeverityIssue() {
			ValidationIssue issue = ValidationIssue.info(id("test"), "Test info");

			assertEquals(ValidationSeverity.INFO, issue.severity());
		}
	}

	@Nested
	@DisplayName("Formatting")
	class FormattingBehavior {

		@Test
		@DisplayName("format includes severity")
		void formatIncludesSeverity() {
			ValidationIssue issue = ValidationIssue.error(id("test"), "Test message");

			String formatted = issue.format();

			assertTrue(formatted.contains("[ERROR]"));
		}

		@Test
		@DisplayName("format includes component ID")
		void formatIncludesComponentId() {
			ValidationIssue issue = ValidationIssue.error(id("my_component"), "Test message");

			String formatted = issue.format();

			assertTrue(formatted.contains("my_component"));
		}

		@Test
		@DisplayName("format includes location when present")
		void formatIncludesLocationWhenPresent() {
			ValidationIssue issue = ValidationIssue.error(
					id("test"), "Test message", "properties.attr[0]");

			String formatted = issue.format();

			assertTrue(formatted.contains("at properties.attr[0]"));
		}

		@Test
		@DisplayName("format includes suggestion with FIX prefix")
		void formatIncludesSuggestionWithFixPrefix() {
			ValidationIssue issue = ValidationIssue.error(
					id("test"), "Test message", "location", "Add missing attribute");

			String formatted = issue.format();

			assertTrue(formatted.contains("FIX: Add missing attribute"));
		}

		@Test
		@DisplayName("format includes message")
		void formatIncludesMessage() {
			ValidationIssue issue = ValidationIssue.error(id("test"), "This is the error message");

			String formatted = issue.format();

			assertTrue(formatted.contains("This is the error message"));
		}
	}
}
