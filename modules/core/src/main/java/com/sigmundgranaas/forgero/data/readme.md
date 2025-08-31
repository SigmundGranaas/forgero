## Core Data Architecture: A Pipeline Overview

The `data` folder in Forgero Core is the backbone of the mod's content, responsible for loading, processing, and generating all item definitions from raw JSON files into functional runtime components. It is structured as a clear, sequential pipeline, orchestrated entirely within the `ForgeroDataInitializer`.

### The Data Initialization Pipeline

The pipeline, executed by `ForgeroDataInitializer`, follows these distinct stages:

1.  **Setup and Raw Data Loading:**
    *   **Purpose:** To read all JSON files from the `data/forgero/` resource directories (`materials`, `shapes`, `parts`, etc.) and parse them into basic Data Transfer Objects (DTOs) like `MaterialData` and `PartTemplateData`.
    *   **Mechanism:** It uses a map of `com.mojang.serialization.Codec` instances to handle different file types. Each successfully parsed file is wrapped in a `RawDefinition` record, which pairs the DTO with its canonical ID.

2.  **Tag Graph Loading:**
    *   **Purpose:** To load all tag definitions from the `data/forgero/tags/` directory.
    *   **Mechanism:** The `TagLoadingService` processes these files to build a `TagGraph`, which is essential for lookups and filtering components based on their tags.

3.  **Static Component Processing:**
    *   **Purpose:** To process all non-template definitions (materials, shapes, static parts).
    *   **Mechanism:**
        *   **`IncludeResolver`**: For each static definition, this service recursively resolves its `include` chain, gathering a list of all parent DTOs.
        *   **`PropertyMerger`**: This service takes the resolved list of DTOs and merges their properties (tags, attributes, etc.) into a single, consolidated set. It correctly handles property overrides and ensures static components receive all their intended tags.
        *   **`CofComponentConverter`**: The merged properties are then converted into a `CofComponent`, which is the canonical intermediate representation of a component before it becomes a runtime object.

4.  **Template-Based Component Generation:**
    *   **Purpose:** To generate all possible composite items (like `iron-pickaxe_head` or `diamond-chestplate`) by combining the already-processed static components based on templates.
    *   **Mechanism (`TemplateGenerator`):**
        *   It iterates through all `PartTemplateData` and `EquipmentTemplateData` files.
        *   For each template, it finds all valid combinations of static components that satisfy the template's slot requirements (e.g., finding all components with the `forgero:tool_material` tag for a "material" slot).
        *   For each valid combination, it generates a new `CofComponent`, resolving its new ID and merging properties. The `PropertyMerger` is used here with special logic to ensure the new composite part only inherits tags from its *template*, not from its constituent ingredients (like materials or shapes).

5.  **Final Component Construction:**
    *   **Purpose:** To transform the complete collection of `CofComponent` DTOs (both static and generated) into final, live `Component` objects.
    *   **Mechanism (`ComponentBuilder`):**
        *   It uses a `ComponentConstructor` registry, which maps type identifiers (e.g., `"forgero:structured_part"`) to factory functions that can create the correct Java class (e.g., `StructuredPart.class`).
        *   It recursively builds all components, resolving dependencies between them (e.g., building the `iron-pickaxe_head` before building the `iron-pickaxe` that uses it).

6.  **Final Bundle Creation:**
    *   **Purpose:** To package the results into a usable data structure.
    *   **Mechanism:** All the final `Component` objects are registered into a `TaggedRegistry`, which is then packaged into a `ForgeroDataBundle` along with the `TagGraph` and host item mappings. This bundle is the final output of the entire pipeline, ready for use by the rest of the mod.

This refined architecture provides a clear, stage-by-stage progression of data transformation. Each service in the pipeline contributes a well-defined piece to the puzzle, improving modularity, testability, and the overall understanding of the data flow.
