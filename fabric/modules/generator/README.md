# Generator module

This module contains utilities and registries required for creating data templates in data directories, which will
automatically get build depending on the runtime configuration.

## Recipe generators

Forgero utilizes recipe generators to create a large amount of similar recipes, using only some defined variables.
The files are located in the data/<namespace>/recipe_generators directory.

### Example

This example will generate one recipe for every tool variable, combined with every material variable.
This recipe ensures all normal tools provided in Forgero has a default crafting recipe.

```json
{
  "generator_type": "forgero:mapped_recipe_generator",
  "identifier": "forgero:${material.name}-${tool}-state-crafting",
  "variables": {
    "material": {
      "type": "TOOL_MATERIAL"
    },
    "tool": [
      "axe",
      "pickaxe",
      "shovel",
      "hoe"
    ]
  },
  "type": "forgero:state_crafting_recipe",
  "pattern": [
    " a",
    "b "
  ],
  "key": {
    "a": {
      "tag": "forgero:${material.name}-${tool}_head"
    },
    "b": {
      "tag": "forgero:handle"
    }
  },
  "result": {
    "item": "forgero:${material.name}-${tool}",
    "count": 1
  }
}
```

### Variables and other keywords

The variables is a key map of variables that should be expanded into more recipes. The key is the variable name, and the
values are determined by Variable factories that can be plugged into the system. By default, only normal string lists
are
enabled, but Forgero adds factories for `TYPE` variables as well as expanding them from tags. The amount of recipes will
grow depending on the size of your variables, and how many variables you define.

This example will generate 4 recipes for every tool type times the amount of materials registered in the `TOOL_MATERIAL`
type.

The identifier is the identifier of the recipe, and will be used to generate a unique identifier for every recipe. The
system will throw errors if it finds duplicate identifiers.

${tool} and ${material.name} are variables that will be replaced with the string based operations on variables. The
syntax is
`${variable_name.operation}`. If there is no operation, the variable will use the `toString()` method on the variable.

### Registering variables and operations

Variable converters and operations can be registered via code.
Here's an example of how Forgero registers its own variables and operations:

```java
private void register(){
		Function<State, String> idConverter=s->StateService.INSTANCE.getMapper().stateToContainer(s.identifier()).toString();

		Function<State, String> tagOrItem=(state)->Registry.ITEM.get(StateService.INSTANCE.getMapper().stateToContainer(state.identifier()))==Items.AIR?"tag":"item";
		Function<State, String> material=(state)->state instanceof MaterialBased based?based.baseMaterial().name():"";

		var factory=new OperationFactory<>(State.class);

		operation("forgero:state_name","name",factory.build(Identifiable::name));
		operation("forgero:state_namespace","namespace",factory.build(Identifiable::nameSpace));
		operation("forgero:state_material","material",factory.build(material));
		operation("forgero:state_identifier","identifier",factory.build(idConverter));
		operation("forgero:state_identifier","id",factory.build(idConverter));
		operation("forgero:tag_or_item","tagOrItem",factory.build(tagOrItem));

		Function<String, List<State>>stateFinder=(type)->ForgeroStateRegistry.TREE.find(Type.of(type))
		.map(node->node.getResources(State.class))
		.orElse(ImmutableList.<State>builder()
		.build());

		ForgeroTypeVariableConverter typeConverter=new ForgeroTypeVariableConverter(stateFinder);

		variableConverter("forgero:type_converter",typeConverter);
		}
```
