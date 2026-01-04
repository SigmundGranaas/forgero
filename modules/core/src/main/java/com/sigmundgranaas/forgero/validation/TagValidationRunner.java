package com.sigmundgranaas.forgero.validation;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.common.tags.validation.TagValidator;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * CLI tool for validating tag definitions.
 * Used by Gradle validateTags task.
 */
public class TagValidationRunner {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: TagValidationRunner <project-dir>");
			System.err.println("  <project-dir>  Root directory of the Forgero project");
			System.exit(1);
		}

		Path projectDir = Paths.get(args[0]);
		System.out.println("[TagValidation] Starting validation in project: " + projectDir);

		try {
			TagResolver resolver = loadAllTags(projectDir);

			int tagCount = resolver.getAllTags().size();
			System.out.println("[TagValidation] Loaded " + tagCount + " tags from content modules");

			// Validate
			var validator = new TagValidator(resolver);
			var result = validator.validate();

			// Print report
			System.out.println(result.formatReport());

			// Exit with error code if validation failed
			if (result.hasErrors()) {
				System.err.println("\n[TagValidation] FAILED: " + result.errors().size() + " errors found");
				System.exit(1);
			} else if (result.hasWarnings()) {
				System.out.println("\n[TagValidation] PASSED with " + result.warnings().size() + " warnings");
				System.exit(0);
			} else {
				System.out.println("\n[TagValidation] PASSED: All tags valid");
				System.exit(0);
			}
		} catch (Exception e) {
			System.err.println("[TagValidation] FATAL: Unexpected error during validation");
			System.err.println("  Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static TagResolver loadAllTags(Path projectDir) {
		IdentifierFactory factory = new IdentifierFactory.Builder()
			.defaultNamespace("forgero")
			.build();
		List<TagResolver> resolvers = new ArrayList<>();

		// Find all content modules
		String[] contentModules = {
			"content/forgero-base",
			"content/forgero-extended",
			"content/forgero-extended-weapons",
			"content/forgero-armor",
			"content/forgero-armor-content",
			"content/forgero-materials",
			"content/forgero-gems",
			"content/forgero-mining",
			"content/forgero-tools",
			"content/forgero-test"
		};

		for (String modulePath : contentModules) {
			Path moduleDir = projectDir.resolve(modulePath);
			Path dataDir = moduleDir.resolve("src/main/resources/data");

			if (!dataDir.toFile().exists()) {
				continue;
			}

			try {
				ResourceProvider provider = new ClassPathResourceProvider(dataDir);
				TagLoadingService loader = new TagLoadingService(factory, provider);

				// Load tags from this module
				TagResolver moduleResolver = loader.loadTags(OpenIdentifier.of("forgero", "tags"));
				resolvers.add(moduleResolver);

				System.out.println("  [OK] " + modulePath + ": " + moduleResolver.getAllTags().size() + " tags");
			} catch (Exception e) {
				System.err.println("  [FAIL] " + modulePath + ": " + e.getMessage());
			}
		}

		// Merge all resolvers
		return resolvers.stream()
			.reduce(TagResolver.empty(), TagResolver::merge);
	}
}
