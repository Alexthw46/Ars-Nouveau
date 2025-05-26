package com.hollingsworth.arsnouveau.api.particle.configurations.properties;

import com.hollingsworth.arsnouveau.api.particle.configurations.ParticleConfigWidgetProvider;
import com.hollingsworth.arsnouveau.api.registry.ParticlePropertyRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseProperty<T extends BaseProperty<T>> {

    public PropMap propertyHolder;

    public BaseProperty(PropMap propertyHolder) {
        this.propertyHolder = propertyHolder;
    }

    public BaseProperty(){
        this.propertyHolder = new PropMap();
    }

    public ResourceLocation getId(){
        return ParticlePropertyRegistry.PARTICLE_PROPERTY_REGISTRY.getKey(getType());
    }

    public Component getName(){
        return Component.translatable(getId().getNamespace() + ".particle.property." + getId().getPath());
    }

    abstract public ParticleConfigWidgetProvider buildWidgets(int x, int y, int width, int height);

    abstract public IPropertyType<T> getType();

    public void writeChanges(){
        if(propertyHolder != null){
            propertyHolder.set(getType(), (T) this);
        }
    }
}
