package com.sigmundgranaas.forgero.quilt.client;


import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

@Environment(value = EnvType.CLIENT)
public class SoulEntityRenderer extends MobEntityRenderer<SoulEntity, SoulEntityModel<SoulEntity>> {
	private static final Identifier PASSIVE_TEXTURE = new Identifier("textures/entity/bee/bee.png");


	public SoulEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new SoulEntityModel<>(context.getPart(EntityModelLayers.BEE)), 0.4f);
	}

	@Override
	public Identifier getTexture(SoulEntity soulEntity) {
		return PASSIVE_TEXTURE;
	}

}
