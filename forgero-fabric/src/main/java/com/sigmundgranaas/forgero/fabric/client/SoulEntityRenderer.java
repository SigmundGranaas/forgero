package com.sigmundgranaas.forgero.fabric.client;


import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

@Environment(value = EnvType.CLIENT)
public class SoulEntityRenderer extends EntityRenderer<SoulEntity> {

    private static final Identifier TEXTURE = new Identifier("textures/entity/experience_orb.png");
    private static final RenderLayer LAYER = RenderLayer.getItemEntityTranslucentCull(TEXTURE);

    protected SoulEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.15f;
        this.shadowOpacity = 0.75f;
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float y, int red, int green, int blue, float u, float v, int light) {
        vertexConsumer.vertex(positionMatrix, x, y, 0.0f).color(red, green, blue, 128).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0f, 1.0f, 0.0f).next();
    }

    @Override
    public Identifier getTexture(SoulEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(SoulEntity soulEntity, BlockPos blockPos) {
        return MathHelper.clamp(super.getBlockLight(soulEntity, blockPos) + 7, 0, 15);
    }

    @Override
    public void render(SoulEntity soulEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        int j = 10;
        float h = (float) (j % 4 * 16 + 0) / 64.0f;
        float k = (float) (j % 4 * 16 + 16) / 64.0f;
        float l = (float) (j / 4 * 16 + 0) / 64.0f;
        float m = (float) (j / 4 * 16 + 16) / 64.0f;
        float n = 1.0f;
        float o = 0.5f;
        float p = 0.25f;
        float q = 255.0f;
        float r = ((float) 6 + g) / 2.0f;
        int s = (int) ((MathHelper.sin(r + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int t = 255;
        int u = (int) ((MathHelper.sin(r + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        matrixStack.translate(0.0, 0.1f, 0.0);
        matrixStack.multiply(this.dispatcher.getRotation());
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));
        float v = 0.3f;
        matrixStack.scale(0.3f, 0.3f, 0.3f);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
        MatrixStack.Entry entry = matrixStack.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();
        SoulEntityRenderer.vertex(vertexConsumer, matrix4f, matrix3f, -0.5f, -0.25f, s, 255, u, h, m, i);
        SoulEntityRenderer.vertex(vertexConsumer, matrix4f, matrix3f, 0.5f, -0.25f, s, 255, u, k, m, i);
        SoulEntityRenderer.vertex(vertexConsumer, matrix4f, matrix3f, 0.5f, 0.75f, s, 255, u, k, l, i);
        SoulEntityRenderer.vertex(vertexConsumer, matrix4f, matrix3f, -0.5f, 0.75f, s, 255, u, h, l, i);
        matrixStack.pop();
        super.render(soulEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
