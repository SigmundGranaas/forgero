# Forgero Documentation Standards

Guidelines for documenting Forgero's JSON APIs and systems.

---

## Documentation Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Quick Reference (docs/JSON_API_QUICK_REFERENCE.md)     │
│  - Field tables, type lists, one-liners                 │
│  - For: Quick lookups while coding                      │
├─────────────────────────────────────────────────────────┤
│  Developer Guides (root directory)                      │
│  - RESOURCE_PACK_DEVELOPER_GUIDE.md (data API)          │
│  - Full examples, use cases, explanations               │
│  - For: Learning the system                             │
├─────────────────────────────────────────────────────────┤
│  Module READMEs (modules/*/README.md)                   │
│  - Module-specific documentation                        │
│  - DTO schemas, API usage, testing                      │
│  - For: Module-level implementation                     │
├─────────────────────────────────────────────────────────┤
│  JSON Schemas (schemas/*.schema.json)                   │
│  - IDE autocomplete and validation                      │
│  - For: Tooling support                                 │
├─────────────────────────────────────────────────────────┤
│  DTO Javadoc (source code)                              │
│  - Implementation details, merge semantics              │
│  - For: Code-level understanding                        │
└─────────────────────────────────────────────────────────┘
```

---

## JSON API Documentation Checklist

When documenting a new JSON type, ensure:

### 1. Quick Reference Entry
Add to `docs/JSON_API_QUICK_REFERENCE.md`:
- Type ID in type tables
- File location
- One-line description

### 2. Developer Guide Section
Add to `RESOURCE_PACK_DEVELOPER_GUIDE.md`:
- Full field table with types, required status, defaults
- Complete JSON structure example
- Practical use case examples

### 3. Module README (if applicable)
Add to relevant `modules/*/README.md`:
- DTO schema (TypeScript format)
- Merge semantics table
- Code examples

### 4. JSON Schema (optional)
Create `schemas/forgero-{type}.schema.json`:
- Full JSON Schema draft-07 definition
- Property descriptions
- Pattern validation

### 5. DTO Javadoc
Add to Java record:
- Field descriptions
- Merge semantics (for extensions)
- Default values
- Constraints

---

## Field Documentation Format

Always use this table format for fields:

```markdown
| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `name` | string | Yes | - | Unique identifier |
| `priority` | int | No | 0 | Lower values applied first |
| `tags` | string[] | No | - | Tags to add to component |
```

### Field Types
- Use TypeScript-style types: `string`, `number`, `boolean`, `string[]`, `object`
- Use `?` suffix for optional: `priority?: number`
- Reference other DTOs: `LayerDTO[]`, `TexturesDTO`

### Required/Default
- **Yes**: Field must be present
- **No**: Field is optional
- **-**: No default (must be provided or null)
- Value: Specific default value

---

## Example Documentation Format

Every JSON type should have three example tiers:

### Minimal Example
Required fields only - shows the simplest valid JSON:

```json
{
  "type": "forgero:material",
  "name": "Example"
}
```

### Complete Example
All fields demonstrated:

```json
{
  "type": "forgero:material",
  "name": "Example",
  "include": ["forgero:materials/base"],
  "tags": ["forgero:metal"],
  "local_tags": ["custom:special"],
  "attributes": [...],
  "local_attributes": [...],
  "properties": {...}
}
```

### Use Case Example
Real-world scenario with context:

```markdown
### Adding Fire Damage to Iron Tools

Add a fire effect to all iron tools when hitting undead:

\`\`\`json
{
  "type": "forgero:extension",
  "target": "forgero:materials/iron",
  "properties": {
    "minecraft:on_hit": [{...}]
  }
}
\`\`\`
```

---

## Code-to-Docs Mapping

| Code Location | Documentation Location |
|---------------|------------------------|
| `*Data.java` records (core module) | RESOURCE_PACK_DEVELOPER_GUIDE.md |
| `*DTO.java` records (model module) | modules/model/README.md |
| `*Codecs.java` | Javadoc in codec file |
| New type added | Quick Reference tables |
| New extension system | Extensions section in guide |

---

## Cross-Reference Guidelines

### Link Format
Use relative markdown links:
```markdown
See [Model Extensions](../modules/model/README.md#model-extensions) for details.
```

### When to Cross-Reference
- Quick Reference → Guide sections (for "see detailed docs")
- Guide sections → Module READMEs (for implementation details)
- Module READMEs → DTO Javadoc (for merge semantics)
- Guide sections → Other guide sections (for related concepts)

### Avoid
- Circular references without clear path
- Dead links (validate before committing)
- Duplicating content instead of linking

---

## Merge Semantics Documentation

For any extension or merge-capable system, document:

### 1. Strategy Table

| Field | Strategy |
|-------|----------|
| `layers` | Concatenate (appended) |
| `slots` | By ID: extension wins |

### 2. Priority Explanation
- What priority value means
- What happens with equal priorities
- Any special handling

### 3. Conflict Behavior
- Warnings logged
- Override behavior
- How to debug conflicts

---

## JSON Schema Guidelines

### File Naming
`schemas/forgero-{type-name}.schema.json`

Examples:
- `forgero-extension.schema.json`
- `forgero-model-extension.schema.json`
- `forgero-material.schema.json`

### Required Fields
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "$id": "https://forgero.sigmundgranaas.com/schemas/{type}.json",
  "title": "Forgero {Type Name}",
  "description": "One-line description",
  "type": "object",
  "required": ["type", ...],
  "properties": {...}
}
```

### Pattern Validation
Use patterns for identifiers:
```json
"target": {
  "type": "string",
  "pattern": "^[a-z_]+:[a-z_/]+$"
}
```

---

## Javadoc Standards for DTOs

### Record Documentation
```java
/**
 * Data transfer object for model extensions.
 *
 * <p>Model extensions add visual elements (layers, slots, mount points)
 * to existing models without modifying original files.</p>
 *
 * <h3>Merge Semantics:</h3>
 * <ul>
 *   <li><strong>Layers:</strong> Concatenated (appended after target)</li>
 *   <li><strong>Slots:</strong> By ID, extension wins with warning</li>
 *   <li><strong>MountPoints:</strong> By name, extension wins with warning</li>
 * </ul>
 *
 * @param type Type identifier, always "forgero:model_extension"
 * @param target Model ID to extend (required)
 * @param priority Merge priority, lower applied first (default: 0)
 * @param layers Layers to append
 * @param slots Slots to add/override by ID
 * @param mountPoints Mount points to add/override by name
 */
public record ModelExtensionDTO(
    String type,
    String target,
    int priority,
    @Nullable List<LayerDTO> layers,
    @Nullable List<SlotDTO> slots,
    @Nullable List<MountPointDTO> mountPoints
) {}
```

---

## Versioning and Changelog

### When Adding New Features
1. Update documentation alongside code
2. Note API additions in CHANGELOG.md
3. Mark deprecated features with `@deprecated` and migration guide

### Deprecation Notice Format
```markdown
> **Deprecated**: `old_field` is deprecated. Use `new_field` instead.
> Migration: Change `"old_field": value` to `"new_field": value`.
> Will be removed in: v2.0
```

---

## Review Checklist

Before merging documentation changes:

- [ ] All code examples are valid JSON
- [ ] Field tables match actual DTO/codec implementation
- [ ] Cross-references link to existing sections
- [ ] Quick Reference updated for new types
- [ ] Examples are tested/verified to work
- [ ] No placeholder text remaining
- [ ] Consistent formatting with existing docs
