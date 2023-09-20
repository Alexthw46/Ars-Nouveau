package com.hollingsworth.arsnouveau.client.renderer.entity;

import com.hollingsworth.arsnouveau.ArsNouveau;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class WhirlisprigModel<T extends LivingEntity & GeoAnimatable> extends GeoModel<T> {

    @Override
    public void setCustomAnimations(T entity, long uniqueID, @Nullable AnimationState<T> customPredicate) {
        super.setCustomAnimations(entity, uniqueID, customPredicate);
        CoreGeoBone head = this.getAnimationProcessor().getBone("head");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(extraData.headPitch() * 0.010453292F);
        head.setRotY(extraData.netHeadYaw() * 0.015453292F);
    }

    @Override
    public ResourceLocation getModelResource(T whirlisprig) {
        return new ResourceLocation(ArsNouveau.MODID, "geo/whirlisprig.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T whirlisprig) {
        return new ResourceLocation(ArsNouveau.MODID, "textures/entity/whirlisprig.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T whirlisprig) {
        return new ResourceLocation(ArsNouveau.MODID, "animations/whirlisprig_animations.json");
    }
}
