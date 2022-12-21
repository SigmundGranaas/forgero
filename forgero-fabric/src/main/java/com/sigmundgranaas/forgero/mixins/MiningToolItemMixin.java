package com.sigmundgranaas.forgero.mixins;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MiningToolItem.class)
public class MiningToolItemMixin {

    @Mutable
    @Final
    @Shadow
    private float attackDamage;

    @Mutable
    @Final
    @Shadow
    private Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<?> effectiveBlocks, Item.Settings settings, CallbackInfo ci) {
        if (this instanceof State) {
            this.attackDamage = attackDamage;
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();

            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(attributeModifiers.get(EntityAttributes.GENERIC_ATTACK_DAMAGE).stream().findFirst().get().getId(), "Tool modifier", this.attackDamage, EntityAttributeModifier.Operation.ADDITION));


            if (attributeModifiers.get(EntityAttributes.GENERIC_ATTACK_SPEED).stream().findAny().isPresent()) {
                builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(attributeModifiers.get(EntityAttributes.GENERIC_ATTACK_SPEED).stream().findFirst().get().getId(), "Tool modifier", attackSpeed, EntityAttributeModifier.Operation.ADDITION));
            }
            this.attributeModifiers = builder.build();
        }
    }
}
