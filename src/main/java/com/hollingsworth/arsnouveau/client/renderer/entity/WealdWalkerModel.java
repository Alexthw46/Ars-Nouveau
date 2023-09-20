package com.hollingsworth.arsnouveau.client.renderer.entity;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.common.entity.WealdWalker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class WealdWalkerModel<W extends WealdWalker> extends GeoModel<W> {
    String type;

    public WealdWalkerModel(String type) {
        super();
        this.type = type;
    }

    @Override
    public void setCustomAnimations(W entity, long uniqueID, @Nullable AnimationState<W> customPredicate) {
        super.setCustomAnimations(entity, uniqueID, customPredicate);

        CoreGeoBone head = this.getAnimationProcessor().getBone("head");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(extraData.headPitch() * 0.010453292F);
        head.setRotY(extraData.netHeadYaw() * 0.015453292F);
        if (entity.getEntityData().get(WealdWalker.CASTING)) {
            CoreGeoBone frontLeftLeg = this.getAnimationProcessor().getBone("leg_right");
            CoreGeoBone frontRightLeg = this.getAnimationProcessor().getBone("leg_left");
            frontLeftLeg.setRotX(Mth.cos(entity.walkAnimation.position() * 0.6662F) * 1.4F * entity.walkAnimation.speed());
            frontRightLeg.setRotX(Mth.cos(entity.walkAnimation.position() * 0.6662F + (float) Math.PI) * 1.4F * entity.walkAnimation.speed());
        }
    }

    @Override
    public ResourceLocation getModelResource(WealdWalker walker) {
        return walker.isBaby() ? new ResourceLocation(ArsNouveau.MODID, "geo/" + type + "_waddler.geo.json") : new ResourceLocation(ArsNouveau.MODID, "geo/" + type + "_walker.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WealdWalker walker) {
        return walker.isBaby() ? new ResourceLocation(ArsNouveau.MODID, "textures/entity/" + type + "_waddler.png") : new ResourceLocation(ArsNouveau.MODID, "textures/entity/" + type + "_walker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WealdWalker walker) {
        return walker.isBaby() ? new ResourceLocation(ArsNouveau.MODID, "animations/weald_waddler_animations.json") : new ResourceLocation(ArsNouveau.MODID, "animations/weald_walker_animations.json");
    }
}
