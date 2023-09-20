package com.hollingsworth.arsnouveau.client.renderer.entity;

import com.hollingsworth.arsnouveau.common.entity.AmethystGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtils;


public class AmethystGolemRenderer extends GeoEntityRenderer<AmethystGolem> {
    public AmethystGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AmethystGolemModel<>());
    }

    AmethystGolem golem;
    MultiBufferSource buffer;
    ResourceLocation text;

    @Override
    public RenderType getRenderType(AmethystGolem animatable, ResourceLocation texture, @org.jetbrains.annotations.Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, AmethystGolem animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.golem = animatable;
        this.buffer = bufferSource;
        this.text = this.getTextureLocation(animatable);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void renderRecursively(PoseStack stack, AmethystGolem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer bufferIn, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("item")) {
            stack.pushPose();
            RenderUtils.translateToPivotPoint(stack, bone);
            stack.translate(0, -0.10, 0);
            ItemStack itemstack = golem.getHeldStack();
            Minecraft.getInstance().getItemRenderer().renderStatic(itemstack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, stack, this.buffer, animatable.level, (int) golem.getOnPos().asLong());
            stack.popPose();
            bufferIn = buffer.getBuffer(RenderType.entityCutoutNoCull(text));
        }
        super.renderRecursively(stack, animatable, bone, renderType, bufferSource, bufferIn, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

    }

}
