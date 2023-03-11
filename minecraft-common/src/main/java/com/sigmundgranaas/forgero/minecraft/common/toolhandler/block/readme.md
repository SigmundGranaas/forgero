# Block handling

This module contains the code for handling blocks. It is used by the `ToolHandler` to determine if a block is
harvestable, and if so, what tool is required to harvest it.
The handlers will also determine which selection of blocks are supposed to be handled by any given tool.

## Selectors

Selector are used to find patterns, groups or veins of valid blocks that should be handled by a tool. This makes it
possible for tools to implement vein mining, 3x3 or custom patterns, as well as column mining.
More selectors can be added to provide even more functionality.

Available selectors:

* `ColumnSelector` - Selects a column of blocks.
* `RadiusVeinSelector` - Selects a vein of blocks.
* `PatternSelector` - Selects a pattern of blocks.

## Hardness

The hardness of a block is used to determine how long it takes to break a block.
Due to fact that selectors can select more than one block, it also makes sense to calculate hardness for more than one
block. This is to prevent vein mining functionality from being too fast.
When used to calculate the time to break a block, the hardness of the block is multiplied by the number of blocks
provided by the selector, each individual blocks' hardness is taken into account when calculating the hardness of a
selection.

## Caching

Calculating selections can be very expensive for larger sets of blocks. To prevent this, the `BlockHandler` will
cache the results of the selectors. This means that the same selection will be returned if the same parameters are
provided.
The cache is time sensitive, and will expire after a short amount of time. This is to prevent the cache from using
invalid results.
After a block has been broken, it will clear caches to make sure that no invalid results are returned.
The caching should ideally be used only for tasks that are run very often, like calculating the block breaking delta for
every tick.
