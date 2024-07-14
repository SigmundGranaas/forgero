## Predicates

Predicates are a generalized system for performing yes/no checks based on certain criteria. They can be used in
attributes to check if an attribute should be applied, or used in Features to check various conditions. Here are two
examples:

Usage in attributes to check if a player is sneaking:

```json
 {
  "id": "sneaking-attack_damage-composite",
  "type": "ATTACK_DAMAGE",
  "order": "BASE",
  "context": "COMPOSITE",
  "operation": "MULTIPLICATION",
  "value": 1.5,
  "predicate": {
    "type": "minecraft:entity",
    "flag": {
      "is_sneaking": true
    }
  }
}
```

Or in features to check if a block is valid for a tool's vein mining features:

```json
{
  "type": "minecraft:block_breaking",
  "selector": {
    "type": "forgero:radius",
    "filter": [
      "forgero:can_mine",
      {
        "type": "minecraft:block",
        "tag": "forgero:vein_mining_ores"
      }
    ],
    "radius": 1
  },
  "speed": "forgero:all",
  "predicate": {
    "type": "minecraft:block",
    "tag": "forgero:vein_mining_ores"
  },
  "title": "feature.forgero.vein_mining.title",
  "description": "feature.forgero.ore_vein_mining.description"
}
```

In this case it is used both in the base `predicate` field as well as one of the filters.

### Json usage

The new Codec based system is primarily based on plug-ins for providing the actual check that should be performed, and
making sure this is available in the json structure.

#### Block

This is the basic form of a json block predicate.
This will create a predicate, but it will not check anything because there are no predicates to check.

```json
{
  "type": "minecraft:block"
}
```

There are currently two supplied predicates for dealing with block conditions: `block` and `tag.`
Both of these can be either a single identifier, or a list of Identifiers which can be true.
When this predicate is used on a block, it will check if the block either matches the given block id, or is in the tag.

Checking if a block matches a specific id:

```json
{
  "type": "minecraft:block",
  "block": "minecraft:coal_ore"
}
```

Checking if a block matches any of the block id's:

```json
{
  "type": "minecraft:block",
  "block": [
    "minecraft:coal_ore",
    "minecraft:deepslate_coal_ore"
  ]
}
```

Checking if a block is in a given tag.

```json
{
  "type": "minecraft:block",
  "tag": [
    "minecraft:coal_ore"
  ]
}
```

#### Entity

The entity predicate will perform check on a given entity. This is the baseline json structure for an entity predicate:

```json
{
  "type": "minecraft:block"
}
```

Using the entity type predicate to check if an entity is a given type:

```json
{
  "type": "minecraft:block",
  "entity_type": {
    "id": "minecraft:pig"
  }
}
```

Using the flag predicate to check for the presence of flags on an entity.

```json
{
  "type": "minecraft:block",
  "flags": {
    "is_sneaking": true
  }
}
```

There are currently four flags that can be checked: `is_sneaking`, `is_running`, `is_swimming` and `is_on_ground`

### API

As an example, we will be using `Entity`, as this is probably the biggest and most relevant predicate.

The main Json block (Everything inside: `{}`) for a predicate type serves as a provider for all the fields in the json.
We are expecting the keys
in the json predicate to be able to take the provided value and perform a condition check on that value alone. We are
treating the entire json predicate block as the following class:
`Predicate<Entity>`. We will be able to register and use any predicate that conforms to this interface. The initial Json
object, identified by: `minecraft:entity` will act as a wrapper around other predicate and make sure to provide a
valid entity that the internal predicates can actually test.

We can then register individual check for an entity, along with a key. We can use the `entity_type` predicate as an
example. The actual check that is performed is
taking an `EntityType` as input, which is not an exact match for `entity`. It is however very easy to convert an entity
to an
entity type, because all entities are required to have an associated type. For this we are using a simple converter that
transforms an `Entity` to a `EntityType` which makes it possible to use the `entity_type` check in the following Json:

```json
{
  "type": "minecraft:entity",
  "entity_type": {
    "id": "minecraft:pig"
  }
}
```

The `entity_type` predicate is registered like
this: `ENTITY_CODEC_REGISTRY.register(KeyPair.pair(ENTITY_TYPE_KEY, EntityAdapter.entityTypePredicate()));`
The keypair makes sure the predicate is associated directly with a key. In this case the key is: `entity_type`, as this
is how we want to use it in Json. We are using an adapter to convert `Entity` to `EntityType`, which makes it possible
to register this into the main Entity registry. Everything in the registry is available to use in Json, which makes it
possible to register a new predicate and use it straight away. As a side note, if the `entity_type` predicate was not
registered, or removed from the registry, it would no longer work in Json.

#### Field predicates and KeyPair

We are treating each Predicate entry as a pair consisting of a String key and predicate. We need this pair for two-way
data serialization using the new `Codec` class. When reading from Json we need to be able to find which Predicate is
connected to which key so that can be parsed correctly, and we need to know which key a predicate should be written to
when are writing it to Json. We don't care about which Json type the key corresponds to, as long as it is a Codec. We
can therefore layer them, and use another layer of key-Predicate pairs which follows the same rule. As an example, we'll
use the `entity_flag` predicate.

We can create individual predicates which are using Entity flags.

```Java
    public static KeyPair<Predicate<Entity>>IS_SNEAKING=predicate("is_sneaking",Entity::isSneaking);
```

These predicates can be registered into the flag registry like this:

```Java
ENTITY_FLAG_PREDICATE_REGISTRY.register(IS_SNEAKING);
```

This flag will then be available to the FlagCodec, which is registered to Base entity codec like this:

```java
ENTITY_CODEC_REGISTRY.register(KeyPair.pair(FlagGroupPredicate.KEY,FlagGroupPredicate.CODEC_SPECIFICATION));
```

It can then be used like this:

```json
{
  "type": "minecraft:entity",
  "flags": {
    "is_sneaking": true
  }
}
```

#### Codecs

Codecs can be a bit complex to work with, as they are abstracting away the logic that maps a given class into something
like a JsonStructure. This can make it a bit harder to understand, as it might not be clear exactly how a codec will
read and write data to a data format like Json. In this system we are using two types of codecs: RegistryBackedCodecs
and Individual type/class codecs.

The registry backed codecs are dynamic objects that will read and write the KeyPair codecs present in the current
registry. The base `EntityCodec` and `BlockCodec` are examples of these codecs. They don't have any default fields and
are depending completely on the Codecs that have been registered for it to use.
They make it possible to create dynamic Json objects with key-predicate pairs that can be implemented/removed/updated
completely independently. Adding a new entry to the registry will allow us to use a new predicate in the json right
away.

Object/field codecs on the other hands have a direct implementation to the class they are supporting.
Here is an example of how the EntityType is implemented.

```java

public record EntityTypePredicate(Identifier identifier) implements Predicate<EntityType<?>> {
	@Override
	public boolean test(EntityType<?> entityType) {
		return Registries.ENTITY_TYPE.get(identifier) == entityType;
	}

	public static final Codec<EntityTypePredicate> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
							Identifier.CODEC.fieldOf("id").forGetter(EntityTypePredicate::identifier))
					.apply(instance, EntityTypePredicate::new));

}
```

This corresponds to the following json:

```json
{
  "id": "minecraft:any_valid_entity_type"
}
```

This direct type of codec implementation is the simplest and most straight forward, but there are a couple of problems.
The class only needs a single identifier, which means that creation of a json object with a single key is completely
redundant. In addition to this, it would be nice if it could take a list of identifiers, so we could check if an
Entity type corresponds to a larger list of Entity types.

Here is example of how this can be done by creating an EitherCodec:

```java
public record TagPredicate(List<TagKey<Block>> tags) implements Predicate<BlockState> {
	@Override
	public boolean test(BlockState blockState) {
		return !tags.isEmpty() && tags.stream().anyMatch(blockState::isIn);
	}

	public static final Codec<TagPredicate> CODEC = Codec.either(
			Identifier.CODEC,
			Codec.list(Identifier.CODEC)
	).xmap(
			TagPredicate::fromEither,
			TagPredicate::toEither
	);

	private static TagPredicate fromEither(Either<Identifier, List<Identifier>> either) {
		return new TagPredicate(either.map(
				id -> Collections.singletonList(identifierToTag(id)),
				TagPredicate::identifiersToTags
		));
	}
```

In this example we are either mapping a single TagKey to a single string, or a list of tagKeys to a list of Strings. We
are using the already created `Identifier.CODEC` to map string into codecs. There are also some utility functions that
are
dealing with mapping identifiers directly to `TagKey<Block>` which has been hidden here.

#### Adapting

As noted earlier in the example, we are creating predicates that act upon a single type, available from a provider.
This makes it possible to create Predicate classes that are tailored to a specific class and nothing else. This is very
flexible, as the predicate does not have to worry about where it is receiving the value it is going to test.
We will use the TagPredicate as an example here. The base `minecraft:block` predicate acts as a provider for the given
class: `WorldBlockPair`. It is essentially a `World` and a `BlockPos`, which is sufficient to gather all kinds of data
about any given block.

This is the basic structure of the TagPredicate:

```Java
public record TagPredicate(List<TagKey<Block>> tags) implements Predicate<BlockState> {
	@Override
	public boolean test(BlockState blockState) {
		return !tags.isEmpty() && tags.stream().anyMatch(blockState::isIn);
	}
}
```

As this is implementing the interface: `Predicate<BlockState>` it will not fit the overall interface of the
BlockPredicate, which is: `Predicate<WorldBlockPair>`. This can easily be adapted, using a method like this:

```Java
public record WorldBlockPair(WorldView world, BlockPos pos) {
	public BlockState state() {
		return world.getBlockState(pos());
	}
}

	public static Function<WorldBlockPair, BlockState> state = WorldBlockPair::state;

	public static Codec<KeyPair<Predicate<WorldBlockPair>>> blockTagAdapter() {
		return AdapterCodec.of(TAG_KEY, generalPredicate(TagPredicate.CODEC, TagPredicate.class), PredicateAdapter.create(Adapters.state));
	}
```

When the TagPredicate is adapted, is properly implements the `Predicate<WorldBlockPair>` interface and can then be
registered as a codec for the `minecraft:block` predicate like this:
`BLOCK_CODEC_REGISTRY.register(TAG_KEY, blockTagAdapter());`
