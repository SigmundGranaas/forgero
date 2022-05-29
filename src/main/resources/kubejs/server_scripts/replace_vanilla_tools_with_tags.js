priority: 1

settings.logAddedRecipes = true
settings.logRemovedRecipes = true
settings.logSkippedRecipes = true
settings.logErroringRecipes = true

onEvent('recipes', event => {

    let vanillaMaterials = ['wooden', 'stone', 'iron', 'golden', 'diamond', 'netherite'];
    let vanillaTools = ['pickaxe', 'shovel', 'hoe', 'sword', 'axe'];


    event.replaceInput({type: 'minecraft:crafting_shapeless'}, "minecraft:stone_pickaxe", "#c:stone_pickaxe")
    event.replaceInput({type: 'minecraft:crafting_shaped'}, '#minecraft:planks', 'minecraft:gold_nugget')

    for (let i = 0; i < vanillaMaterials.length; i++) {
        for (let j = 0; j < vanillaTools.length; j++) {
            //event.replaceInput({type: 'minecraft:crafting_shapeless'}, "".concat('minecraft:', vanillaMaterials[i], '_', vanillaTools[j])), "".concat('#c:', vanillaMaterials[i], '_', vanillaTools[j])

        }
    }
})