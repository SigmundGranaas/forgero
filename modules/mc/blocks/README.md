# Forgero MC Blocks Module

Crafting station blocks for managing upgrades and disassembling components.

## Blocks

- **Upgrade Station**: Install, remove, and swap upgrades in component slots
- **Assembly Station**: Disassemble components into constituent parts

## StationContext

Dependency injection for station operations:

```java
StationContext context = StationContext.create(ForgeroApi.services(), world, pos);

// Access services
ComponentConverter converter = context.converter();
SlotManager slotManager = context.slotManager();
```

## UpgradeOperationHandler

Handles upgrade operations with type-safe results:

```java
UpgradeOperationHandler handler = UpgradeOperationHandler.create(context);

// Install into specific slot
StationOperationResult result = handler.installUpgrade(tool, slotId, gemStack);

// Auto-find compatible slot
result = handler.autoInstall(pickaxe, gemStack);

// Remove upgrade
result = handler.removeUpgrade(tool, slotId);

// Handle result
switch (result) {
    case Success s -> updateUI(s.component(), s.stack());
    case Failure f -> showError(f.toText());
    case NoOp n -> { /* nothing changed */ }
}
```

## StationOperationResult

```java
public sealed interface StationOperationResult {
    record Success(Component component, ItemStack stack) implements StationOperationResult;
    record Failure(String errorKey, Object... args) implements StationOperationResult;
    record NoOp() implements StationOperationResult;
}
```

Common failures: `noCompatibleSlot()`, `slotAlreadyFilled()`, `invalidUpgrade(type)`, `slotNotFound(id)`

## ComponentTreeBuilder

Builds UI tree from component structure:

```java
ComponentTreeBuilder builder = new UpgradeTreeBuilder();
SlotTree tree = builder.buildTree(sword);

tree.traverse(node -> {
    if (node.isUpgradeSlot() && node.isEmpty()) {
        // Available for upgrades
    }
});
```

## SlotLayoutEngine

Calculates 2D positions for tree nodes in UI:

```java
SlotLayoutEngine layout = new SlotLayoutEngine(tree);
SlotPosition pos = layout.getPosition(slotId);
addSlot(new ComponentSlot(inventory, index, pos.x(), pos.y()));
```

## DisassemblyService

Breaks items into parts using two methods:

1. **Component-based**: Forgero components are disassembled into structure parts and upgrades
2. **Recipe-based**: Non-Forgero items are disassembled using JSON recipes

```java
DisassemblyService service = DisassemblyService.create(context);

ItemStack stack = ...; // Any ItemStack (Forgero or vanilla)

if (service.canDisassemble(stack)) {
    DisassemblyResult result = service.disassemble(stack);
    for (ItemStack part : result.parts()) {
        // Add to output slots
    }
}
```

### Disassembly Recipes

Recipe files are located in `data/*/disassembly/*.json`:

```json
{
  "input": "minecraft:diamond_pickaxe",
  "results": [
    "minecraft:diamond",
    "minecraft:diamond",
    "minecraft:diamond",
    "minecraft:stick",
    "minecraft:stick"
  ]
}
```

Recipes are automatically reloaded when resource packs change.
