# Forgero Beta release 0.10.7

## ! Warning !

**This version is not compatible with earlier versions of Forgero: 0.9 and below, do not upgrade to this version unless
you have backed up your save.**

## Changelog 0.10.7

* Fixed sword guard models that were causing some combinations to turn invisible
* Removed SmithingRecipeGettersMixin in favour of access widener
* Added template and material resource overrides
* Fixed crimson and warped wood not having attack damage
* Fixed all basic recipes being changed to use the minecraft namespace for wood variants
* Fixed tool repair in anvil
* Fixed pack dependencies being broken
* Fixed tool material not reflecting the tool
* Added a new unbreakable tool config option
* Tool specific Better Combat integration
* Disassembled materials and other parts that has no modifications will no longer have nbt's
* Fixed redstone gem achievement having the same title as the gem achievement
* Added basic stone upgrade recipe for all parts
* Configuration will now automatically update with new values
* Added new setting to toggle conversion from vanilla tools to forgero tools in loot
* Added toggle to disable vanilla tools from recipes and loot
* Assembly station can now be placed next to non-solid blocks
* Added basic recipes for all wooden types
* Added new conditions section to tooltip
* Added 17 new Conditions that can spawn on looted parts, tools and schematics
* Added a data-pack based system for defining properties added by souls when leveled up
* Added Property multiplier for blocks mined and entities killed. (blocks: 1000 = +1%, mobs: 100 = +1%)
* Added soul totem with the soul binding effects
* Added the bottled soul item, which can be obtained by using a bottle on a soul
* Implemented a data-pack based system to add conditions which can be added to lootable drops
* Added dynamic effectiveness property
* Added dynamic conditions to tools
* Added the Broken conditions property
* Added the unbreakable property
* Implemented the new Souls system into tools
* Implemented new classes for Composites (parts and tools)
* Fixed palette names being directly tied to resource
* Added armor property
* Added The max health addition property
* Added luck property
* Added critical chance property
* Fixed simple sword guard recipe to not conflict with wooden bowl
* Added a ModMenu implementation by Steveplays
* Reworked the configuration system by Steveplays
* Added multi block highlight and multi block breaking textures, depending on your tool
* Implemented a new property system for handling special properties (Now called features)

## Changelog 0.10.6

* Added a new recipe type for Patchouli (State upgrade recipe)
* Added patchouli entry for pommels
* Renamed Pummel to Pommel
* Fixed compat issue with EMI Loot
* Fixed scabbard configuration for some sword blades
* Fixed createstation command to fill up chests with proper items
* Fixed sword guards from the extended package not being able to apply to sword (And added gametest to cover this)
* Added new ruined smithing house structure
* Fixed shovel head gem placement

## Changelog 0.10.5

* Fixed repair kits not working, and added a gametest to assert that it functions correctly
* Added a toggle to convert all vanilla tool recipes to Forgero variants
* Added a toggle to disable vanilla tool recipes
* Fixed assembly station being able to deconstruct damaged items
* New chinese translations by Rad233
* Simplified schematic conversion recipes
* Fixed Texture registry being called when not initialized causing a hard crash
* Added tags for all schematic part types

## Changelog 0.10.4.1

* Fixed patchouli guidebook recipe generator which caused a crash
* Fixed patchouli entries using old recipe format
* Added a gametest to the fabric compat module to check essential compatability
* Added a new recipe renderer for Forgero state recipes to correctly render cycled crafting output

## Changelog 0.10.4

* Fixed various tags which broke some recipes
* Fixed mod dependencies not applying properly causing compatability with other mods to break
* Added support for creating custom parts using resources
* Added tests for common recipes
* Optimised recipe creation process
* Improved performance when mining with forgero tools
* Implemented a lot of caching to help with expensive calculations being made too often
* Fixed crash when viewing tooltip with languages using commas instead of periods for decimals
* Fixed mining level not applying to modded blocks

## Changelog 0.10.3

* Fixed compatability with Roughly enough resources
* Changed data loading to never load disabled packages
* Fixed mining levels on tools
* Added drop to Assembly table
* Added assembly table to mineable tag
* Fixes various issues with duplicated mining speed application and calculations

## Changelog 0.10.2

* Fixed path mining pickaxe pattern mining description
* Implemented basic resource reloading data/assets
* Fixed patchouli advancement only generating when patchouli is loaded
* Improved assembly table model and voxel shape
* Assembly table drops inventory when closed
* Removed custom Forgero tool and weapon groups
* Removed generated oak variants of extended tools

## Changelog 0.10.1

* Removed uses of Fabric resource generators. Replaced with dynamic ARRP generators.
* Fixed mythicmetals integration, and optimised some materials
* Fixed materials count for reinforced handle
* Created a naming split element, to make it easier to negate spacing in names for some languages

## Changelog 0.10.0

* Completely revamped the core design and structure of Forgero
* Basically rewrote the system from scratch ( Yay..... )
* Added several new and unique schematics
* New module and pack systems to handle new content added via Forgero
* Packs added: vanilla, extended, structures and compatability
* New undying totem upgrade available
* New assembly station for taking apart tools
* New structures where the assembly station can be found
* Native support for Sodium(Indium is no longer required)
* Performance improvements when rendering item models
* Repair kits for all materials (craft base kit with leather + iron, then craft repair kit from base kit + desired
  material)
* New materials available (Polar bear pelt)


