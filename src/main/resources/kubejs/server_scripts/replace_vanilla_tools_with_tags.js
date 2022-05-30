priority: 1

settings.logAddedRecipes = true
settings.logRemovedRecipes = true
settings.logSkippedRecipes = false
settings.logErroringRecipes = true

onEvent('recipes', event => {

    let vanillaMaterials = ['wooden', 'stone', 'iron', 'golden', 'diamond', 'netherite'];
    let vanillaTools = ['pickaxe', 'shovel', 'hoe', 'sword', 'axe'];

    for (let i = 0; i < vanillaMaterials.length; i++) {
        for (let j = 0; j < vanillaTools.length; j++) {
            event.replaceInput({
                input: "minecraft:".concat(vanillaMaterials[i], '_', vanillaTools[j]),
                not: {output: "#forgero:head_schematics"}
            }, "minecraft:".concat(vanillaMaterials[i], '_', vanillaTools[j]), '#c:'.concat(vanillaMaterials[i], '_', vanillaTools[j]))
        }
    }
})