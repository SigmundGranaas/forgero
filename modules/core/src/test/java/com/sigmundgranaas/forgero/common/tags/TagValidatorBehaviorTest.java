package com.sigmundgranaas.forgero.common.tags;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.common.tags.validation.TagValidator;
import com.sigmundgranaas.forgero.common.tags.validation.TagValidator.ErrorSeverity;
import com.sigmundgranaas.forgero.common.tags.validation.TagValidator.ValidationResult;
import com.sigmundgranaas.forgero.common.tags.validation.TagValidator.WarningSeverity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tag Validation Behavior")
class TagValidatorBehaviorTest {

	private static final IdentifierFactory ID_FACTORY = new IdentifierFactory.Builder()
			.defaultNamespace("forgero")
			.build();

	private static OpenIdentifier id(String path) {
		return ID_FACTORY.of(path);
	}

	private static TagResolver mockResolver(Set<OpenIdentifier> allTags, 
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap) {
		return new TagResolver() {
			@Override
			public boolean hasTag(Taggable item, OpenIdentifier tag) {
				return item.getTags().contains(tag);
			}

			@Override
			public Set<OpenIdentifier> getDescendants(OpenIdentifier tag) {
				return Set.of(tag);
			}

			@Override
			public Set<OpenIdentifier> getParents(OpenIdentifier tag) {
				return parentMap.getOrDefault(tag, Set.of());
			}

			@Override
			public Set<OpenIdentifier> getAllTags() {
				return allTags;
			}

			@Override
			public <T extends Taggable> List<T> findTagged(OpenIdentifier tag, Collection<T> items) {
				return List.of();
			}

			@Override
			public <T extends Taggable> List<T> findDirectlyTagged(OpenIdentifier tag, Collection<T> items) {
				return List.of();
			}

			@Override
			public TagResolver merge(TagResolver other) {
				return this;
			}

			@Override
			public Map<OpenIdentifier, Set<OpenIdentifier>> getRelationships() {
				return parentMap;
			}
		};
	}

	@Nested
	@DisplayName("Valid Tag Hierarchies")
	class ValidTagHierarchies {

		@Test
		@DisplayName("well-formed hierarchy produces no errors or warnings")
		void wellFormedHierarchyProducesNoErrorsOrWarnings() {
			// Build a proper hierarchy: iron -> metal -> materials (known root)
			Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
			relationships.put(id("iron"), Set.of(id("metal")));
			relationships.put(id("metal"), Set.of(id("materials")));
			relationships.put(id("materials"), Set.of()); // known root tag

			TagResolver resolver = TagResolver.fromRelationships(relationships);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertFalse(result.hasErrors(), "Valid hierarchy should have no errors");
			assertFalse(result.hasWarnings(), "Hierarchy with known root should have no warnings");
		}

		@Test
		@DisplayName("multiple branches converging to same root is valid")
		void multipleBranchesConvergingToSameRootIsValid() {
			// iron -> metal -> materials
			// oak -> wood -> materials
			Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
			relationships.put(id("iron"), Set.of(id("metal")));
			relationships.put(id("oak"), Set.of(id("wood")));
			relationships.put(id("metal"), Set.of(id("materials")));
			relationships.put(id("wood"), Set.of(id("materials")));
			relationships.put(id("materials"), Set.of());

			TagResolver resolver = TagResolver.fromRelationships(relationships);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertFalse(result.hasErrors());
		}

		@Test
		@DisplayName("tag with multiple parents is valid when all parents exist")
		void tagWithMultipleParentsIsValidWhenAllParentsExist() {
			// bronze -> metal, bronze -> alloy
			// metal -> materials, alloy -> materials
			Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
			relationships.put(id("bronze"), Set.of(id("metal"), id("alloy")));
			relationships.put(id("metal"), Set.of(id("materials")));
			relationships.put(id("alloy"), Set.of(id("materials")));
			relationships.put(id("materials"), Set.of());

			TagResolver resolver = TagResolver.fromRelationships(relationships);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertFalse(result.hasErrors());
		}
	}

	@Nested
	@DisplayName("Undefined Parent Detection")
	class UndefinedParentDetection {

		@Test
		@DisplayName("detects reference to undefined parent tag")
		void detectsReferenceToUndefinedParentTag() {
			Set<OpenIdentifier> allTags = Set.of(id("iron"));
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap = new HashMap<>();
			parentMap.put(id("iron"), Set.of(id("undefined_parent")));

			TagResolver resolver = mockResolver(allTags, parentMap);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertTrue(result.hasErrors(), "Should detect undefined parent reference");
			assertEquals(1, result.errors().size());
			assertTrue(result.errors().get(0).message().contains("undefined_parent"),
					"Error message should mention the undefined parent");
		}

		@Test
		@DisplayName("detects multiple undefined parent references")
		void detectsMultipleUndefinedParentReferences() {
			Set<OpenIdentifier> allTags = Set.of(id("iron"), id("gold"));
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap = new HashMap<>();
			parentMap.put(id("iron"), Set.of(id("undefined1")));
			parentMap.put(id("gold"), Set.of(id("undefined2")));

			TagResolver resolver = mockResolver(allTags, parentMap);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertTrue(result.hasErrors());
			assertEquals(2, result.errors().size());
		}

		@Test
		@DisplayName("error includes the tag that has the undefined reference")
		void errorIncludesTheTagWithUndefinedReference() {
			Set<OpenIdentifier> allTags = Set.of(id("my_custom_tag"));
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap = new HashMap<>();
			parentMap.put(id("my_custom_tag"), Set.of(id("nonexistent_parent")));

			TagResolver resolver = mockResolver(allTags, parentMap);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertEquals(id("my_custom_tag"), result.errors().get(0).tag());
			assertEquals(ErrorSeverity.ERROR, result.errors().get(0).severity());
		}
	}

	@Nested
	@DisplayName("Orphaned Tag Detection")
	class OrphanedTagDetection {

		@Test
		@DisplayName("warns about orphaned tags with no parents")
		void warnsAboutOrphanedTagsWithNoParents() {
			// A custom tag with no parents that isn't a known root
			Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
			relationships.put(id("floating_tag"), Set.of());

			TagResolver resolver = TagResolver.fromRelationships(relationships);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertTrue(result.hasWarnings(), "Should warn about orphaned tag");
			assertEquals(1, result.warnings().size());
			assertTrue(result.warnings().get(0).message().contains("Orphaned"));
		}

		@Test
		@DisplayName("does not warn about known root tags")
		void doesNotWarnAboutKnownRootTags() {
			// Known root tags: materials, parts, upgrades, tools, armors, weapons, schematics
			Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
			relationships.put(id("materials"), Set.of());
			relationships.put(id("parts"), Set.of());
			relationships.put(id("tools"), Set.of());

			TagResolver resolver = TagResolver.fromRelationships(relationships);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertFalse(result.hasWarnings(), "Known root tags should not produce warnings");
		}

		@Test
		@DisplayName("warning suggests connecting to category root")
		void warningSuggestsConnectingToCategoryRoot() {
			Map<OpenIdentifier, Set<OpenIdentifier>> relationships = new HashMap<>();
			relationships.put(id("custom_orphan"), Set.of());

			TagResolver resolver = TagResolver.fromRelationships(relationships);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertTrue(result.warnings().get(0).message().contains("category root"));
			assertEquals(WarningSeverity.INFO, result.warnings().get(0).severity());
		}
	}

	@Nested
	@DisplayName("Validation Report Formatting")
	class ValidationReportFormatting {

		@Test
		@DisplayName("report includes error count")
		void reportIncludesErrorCount() {
			Set<OpenIdentifier> allTags = Set.of(id("tag1"), id("tag2"));
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap = new HashMap<>();
			parentMap.put(id("tag1"), Set.of(id("undefined1")));
			parentMap.put(id("tag2"), Set.of(id("undefined2")));

			TagResolver resolver = mockResolver(allTags, parentMap);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();
			String report = result.formatReport();

			assertTrue(report.contains("ERRORS (2)"));
		}

		@Test
		@DisplayName("report includes warning count")
		void reportIncludesWarningCount() {
			Set<OpenIdentifier> allTags = Set.of(id("orphan1"), id("orphan2"));
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap = new HashMap<>();
			parentMap.put(id("orphan1"), Set.of());
			parentMap.put(id("orphan2"), Set.of());

			TagResolver resolver = mockResolver(allTags, parentMap);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();
			String report = result.formatReport();

			assertTrue(report.contains("WARNINGS (2)"));
		}

		@Test
		@DisplayName("report shows success message when valid")
		void reportShowsSuccessMessageWhenValid() {
			Set<OpenIdentifier> allTags = Set.of(id("materials"));
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap = new HashMap<>();
			parentMap.put(id("materials"), Set.of());

			TagResolver resolver = mockResolver(allTags, parentMap);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();
			String report = result.formatReport();

			assertTrue(report.contains("validated successfully"));
		}

		@Test
		@DisplayName("error format includes tag and message")
		void errorFormatIncludesTagAndMessage() {
			Set<OpenIdentifier> allTags = Set.of(id("broken_tag"));
			Map<OpenIdentifier, Set<OpenIdentifier>> parentMap = new HashMap<>();
			parentMap.put(id("broken_tag"), Set.of(id("missing")));

			TagResolver resolver = mockResolver(allTags, parentMap);
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();
			String formatted = result.errors().get(0).format();

			assertTrue(formatted.contains("ERROR"));
			assertTrue(formatted.contains("broken_tag"));
			assertTrue(formatted.contains("missing"));
		}
	}

	@Nested
	@DisplayName("Empty Graph Validation")
	class EmptyGraphValidation {

		@Test
		@DisplayName("empty resolver validates without errors")
		void emptyResolverValidatesWithoutErrors() {
			TagResolver resolver = TagResolver.empty();
			TagValidator validator = new TagValidator(resolver);

			ValidationResult result = validator.validate();

			assertFalse(result.hasErrors());
			assertFalse(result.hasWarnings());
		}
	}
}
