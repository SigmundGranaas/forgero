### Changelog 0.9.4

# Forgero Beta release - THIS RELEASE WILL CRASH IF USED ON SAVES WITH EARLIER FORGERO VERSIONS

* Fixed Gem levels not being applied in ToolPart state
* Fixed Pathcouli entries
* Reworked NBT factory to use Optionals
* Implemented item model caching to improve performance when lots of Forgero models are rendered at the same time
* Fix bug where all players could execute forgero commands ( Identithree )
* Added gem translations
* Added Forgero tools to the banned_uncrafting tag, to resolve bugs and exploits related to uncrafting Forgero tools
* Removed duplicated tool recipes
* Added more translations
* Removed duplicated tool type names
* Added Therrassium support
* Balanced Alloygery and Mythicmetals materials
* Reworked identifier element from _ to -, to make it possible to use _ in resource names, like dark_oak
* Updated all recipes to new identifiers
* Updated Icon for group tab
* Added translations for all schematics
* Made most schematics not unique
* Added saber blade
* Added knife
* Added broad sword
* added tachi
* Added tsubi binding
* Added short sword blade
* Added kimiri binding
* Reworked schematic Schema, to make textures and registered items more configurable
* Fixed a bug where rouge texture identifiers would create an IndexOutOfboundExeption
* Fixed crash with fabric api v0.56
* Removed necessary entrypoint extension used by Forgero via AARP
