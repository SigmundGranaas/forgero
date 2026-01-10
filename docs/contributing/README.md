# Contributing to Forgero

Guide for contributors to the Forgero project.

## Getting Started

1. Fork the repository
2. Clone your fork
3. Run `./gradlew build` to verify setup
4. Create a feature branch

## Development Resources

| Topic | Documentation |
|-------|---------------|
| **Testing** | [testing.md](testing.md) - GameTest patterns and test utilities |
| **Documentation** | [documentation-standards.md](documentation-standards.md) - Writing docs |

## Build Commands

```bash
# Build everything
./gradlew build

# Run tests
./gradlew test

# Run Minecraft client with mod
./gradlew :mods:forgero:runClient

# Run specific module tests
./gradlew :modules:core:test
```

## Code Style

- Java 17 features encouraged
- Use descriptive names
- Prefer immutability
- Document public APIs

## Pull Request Process

1. Ensure tests pass: `./gradlew test`
2. Update documentation if needed
3. Create PR against `1.20` branch
4. Wait for review

## Project Structure

```
forgero/
├── modules/          # Core modules (Java code)
│   ├── core/         # Platform-agnostic core
│   ├── model/        # Model generation
│   └── mc/           # Minecraft-specific modules
├── content/          # JSON content packs
├── mods/             # Mod implementations
└── docs/             # Documentation
```

## See Also

- [Architecture Overview](../architecture/README.md) - System design
- [CLAUDE.md](../../CLAUDE.md) - Comprehensive project context
