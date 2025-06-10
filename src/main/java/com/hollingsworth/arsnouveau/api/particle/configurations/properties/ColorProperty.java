package com.hollingsworth.arsnouveau.api.particle.configurations.properties;

import com.hollingsworth.arsnouveau.api.documentation.DocAssets;
import com.hollingsworth.arsnouveau.api.documentation.DocClientUtils;
import com.hollingsworth.arsnouveau.api.particle.configurations.ParticleConfigWidgetProvider;
import com.hollingsworth.arsnouveau.api.registry.ParticlePropertyRegistry;
import com.hollingsworth.arsnouveau.client.ClientInfo;
import com.hollingsworth.arsnouveau.client.gui.*;
import com.hollingsworth.arsnouveau.client.gui.buttons.ColorPresetButton;
import com.hollingsworth.arsnouveau.client.gui.buttons.SelectedParticleButton;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.client.particle.RainbowParticleColor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ColorProperty extends BaseProperty<ColorProperty> {

    public static MapCodec<ColorProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParticleColor.CODEC.fieldOf("particleColor").forGetter(i -> i.particleColor),
            Codec.BOOL.fieldOf("tintDisabled").orElse(false).forGetter(i -> i.tintDisabled)
    ).apply(instance, ColorProperty::new));

    public static StreamCodec<RegistryFriendlyByteBuf, ColorProperty> STREAM_CODEC = StreamCodec.composite(ParticleColor.STREAM,
            (i) -> i.particleColor,
            ByteBufCodecs.BOOL,
            ColorProperty::isTintDisabled,
            ColorProperty::new);

    public ParticleColor particleColor;
    public boolean isLegacyRGB = false;
    boolean tintDisabled = false;

    public ColorProperty(ParticleColor property, boolean tintDisabled) {
        super();
        this.particleColor = property;
        this.tintDisabled = tintDisabled;
        this.displayColor = particleColor;
    }

    public ColorProperty(){
        this(ParticleColor.defaultParticleColor(), true);
    }

    public ColorProperty usesLegacyRGB(boolean legacyRGB) {
        this.isLegacyRGB = legacyRGB;
        return this;
    }

    public ParticleColor color(){
        return tintDisabled ? ParticleColor.WHITE : particleColor;
    }

    public boolean isTintDisabled(){
        return tintDisabled;
    }

    private ParticleColor displayColor;

    @Override
    public ParticleConfigWidgetProvider buildWidgets(int x, int y, int width, int height) {
        ColorProperty property = this;
        return new ParticleConfigWidgetProvider(x, y, width, height) {
            BookSlider redW;
            BookSlider greenW;
            BookSlider blueW;
            HueSlider hueSlider;
            SatLumSlider saturation;
            SatLumSlider lightness;
            SelectedParticleButton rainbowButton;
            SelectedParticleButton noneButton;
            List<SelectedParticleButton> selectableButtons = new ArrayList<>();

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                DocClientUtils.drawHeader(getName(), graphics, x, y, width, mouseX, mouseY, partialTicks);
                Color color = new Color(displayColor.getColor(), false);

                int xOffset = x + 7;
                int yOffset = y + 18;
                graphics.fill(xOffset + 2, yOffset + 3, xOffset - 2 +  DocAssets.SPELLSTYLE_COLOR_PREVIEW.width(),  yOffset - 3 + DocAssets.SPELLSTYLE_COLOR_PREVIEW.height(), color.getRGB());

                graphics.fill(xOffset + 3, yOffset + 2, xOffset - 3 +  DocAssets.SPELLSTYLE_COLOR_PREVIEW.width(),  yOffset - 2 + DocAssets.SPELLSTYLE_COLOR_PREVIEW.height(), color.getRGB());

                DocClientUtils.blit(graphics, DocAssets.SPELLSTYLE_COLOR_PREVIEW, xOffset, yOffset);
                int hueOffset = 35;
                if(!isLegacyRGB){
                    DocClientUtils.drawParagraph(Component.translatable("ars_nouveau.hue"), graphics, x + 8, y + hueOffset, width, mouseX, mouseY, partialTicks);
                    DocClientUtils.drawParagraph(Component.translatable("ars_nouveau.sat"), graphics, x + 8, y + hueOffset + 20, width, mouseX, mouseY, partialTicks);
                    DocClientUtils.drawParagraph(Component.translatable("ars_nouveau.lightness"), graphics, x + 8, y + hueOffset + 40, width, mouseX, mouseY, partialTicks);
                }else{
                    DocClientUtils.drawParagraph(Component.translatable("ars_nouveau.color_gui.red_slider", redW.getValueInt()), graphics, x + 8, y + hueOffset, width, mouseX, mouseY, partialTicks);
                    DocClientUtils.drawParagraph(Component.translatable("ars_nouveau.color_gui.green_slider", greenW.getValueInt()), graphics, x + 8, y + hueOffset + 20, width, mouseX, mouseY, partialTicks);
                    DocClientUtils.drawParagraph(Component.translatable("ars_nouveau.color_gui.blue_slider", blueW.getValueInt()), graphics, x + 8, y + hueOffset + 41, width, mouseX, mouseY, partialTicks);
                }
            }

            @Override
            public void tick() {
                super.tick();
                if(displayColor != null){
                    displayColor = displayColor.transition(ClientInfo.ticksInGame);
                }
            }

            @Override
            public void addWidgets(List<AbstractWidget> widgets) {
                Consumer<Double> colorChanged = (value) -> {
                    ParticleColor color = new ParticleColor((int)redW.getValue(), (int)greenW.getValue(), (int)blueW.getValue());
                    particleColor = color;
                    displayColor = particleColor;
                    propertyHolder.set(getType(), property);
                };
                int hueOffset = 45;
                redW = buildSlider(x + 10, y + hueOffset - 5, Component.translatable("ars_nouveau.color_gui.red_slider"), Component.empty(), 255, colorChanged);
                greenW = buildSlider(x + 10, y + hueOffset + 15, Component.translatable("ars_nouveau.color_gui.green_slider"), Component.empty(), 25, colorChanged);
                blueW = buildSlider(x + 10, y + hueOffset + 36, Component.translatable("ars_nouveau.color_gui.blue_slider"), Component.empty(), 180, colorChanged);

                int xOffset = 7;
                hueSlider = new HueSlider(x + xOffset, y + hueOffset, false, () -> HSLColor.hsl(hueSlider.getValueInt(), saturation.getValue(), lightness.getValue()), (val) ->{
                    updateParticleColor();
                });
                saturation = new SatLumSlider(x + xOffset, y + hueOffset + 20, false, false, () -> HSLColor.hsl(hueSlider.getValueInt(), saturation.getValue(), lightness.getValue()), (val) -> {
                    updateParticleColor();
                });
                lightness = new SatLumSlider(x + xOffset, y + hueOffset + 40, false, true, () -> HSLColor.hsl(hueSlider.getValueInt(), saturation.getValue(), lightness.getValue()), (val) -> {
                    updateParticleColor();
                });

                setFromPreset(particleColor);
                int numPerRow = 6;
                int size = ParticleColor.PRESET_COLORS.size();
                for(int i = 0; i < size; i++){
                    ParticleColor color = ParticleColor.PRESET_COLORS.get(i);
                    var button = new ColorPresetButton(x + xOffset + (i % numPerRow) * 18, y + 100 + (i / numPerRow) * 18, color, (b) ->{
                        this.setFromPreset(color);
                        updateParticleColor();
                    });
                    selectableButtons.add(button);
                    widgets.add(button);
                }
                rainbowButton = new SelectedParticleButton(x + xOffset + (size % numPerRow) * 18, y + 100 + (size / numPerRow) * 18, DocAssets.SPELLSTYLE_RAINBOW, (button) ->{
                    particleColor = new RainbowParticleColor(particleColor.getRedInt(), particleColor.getGreenInt(), particleColor.getBlueInt());
                    displayColor = particleColor;
                    tintDisabled = false;
                    propertyHolder.set(getType(), property);
                    updateSelected();
                }){
                    @Override
                    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
                        DocClientUtils.blit(graphics, DocAssets.SPELLSTYLE_BUTTON_BIG, x, y);
                        super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
                    }
                };
                size++;
                noneButton = new SelectedParticleButton(x + xOffset + (size % numPerRow) * 18, y + 100 + (size / numPerRow) * 18, DocAssets.STYLE_ICON_NONE, (button) ->{
                    tintDisabled = true;
                    particleColor = ParticleColor.WHITE;
                    displayColor = particleColor;
                    propertyHolder.set(getType(), property);
                    updateSelected();
                });
                rainbowButton.withTooltip(Component.translatable("ars_nouveau.color_rainbow"));
                noneButton.withTooltip(Component.translatable("ars_nouveau.color_none"));
                selectableButtons.add(noneButton);
                selectableButtons.add(rainbowButton);
                widgets.add(rainbowButton);
                if(isLegacyRGB) {
                    widgets.add(redW);
                    widgets.add(greenW);
                    widgets.add(blueW);
                }else{
                    widgets.add(hueSlider);
                    widgets.add(saturation);
                    widgets.add(lightness);
                    widgets.add(noneButton);
                }
            }

            public void updateSelected(){
//                for(SelectedParticleButton selectedParticleButton : selectableButtons){
//                    selectedParticleButton.selected = false;
//                }
//
//                if(tintDisabled){
//                    noneButton.selected = true;
//                }else{
//                    for(SelectedParticleButton selectedParticleButton : selectableButtons){
//                        if(selectedParticleButton instanceof ColorPresetButton presetButton){
//                            if(presetButton.particleColor.equals(particleColor)){
//                                selectedParticleButton.selected = true;
//                                break;
//                            }
//                        } else if(selectedParticleButton == rainbowButton && particleColor instanceof RainbowParticleColor){
//                            selectedParticleButton.selected = true;
//                            break;
//                        } else if(selectedParticleButton == noneButton && tintDisabled){
//                            selectedParticleButton.selected = true;
//                            break;
//                        }
//                    }
//                }
            }

            public void updateParticleColor(){
                tintDisabled = false;
                particleColor = HSLColor.hsl(hueSlider.getValueInt(), saturation.getValue(), lightness.getValue()).toColor().toParticle();
                displayColor = particleColor;
                propertyHolder.set(getType(), property);
                updateSelected();
            }

            public void setFromPreset(ParticleColor preset) {
                redW.setValue(Mth.clamp(preset.getRed() * 255.0, 1, 255));
                greenW.setValue(Mth.clamp(preset.getGreen() * 255.0, 1, 255));
                blueW.setValue(Mth.clamp(preset.getBlue() * 255.0, 1, 255));
                HSLColor color = HSLColor.rgb(preset.getRedInt(), preset.getGreenInt(), preset.getBlueInt());
                hueSlider.setValue(color.getHue());
                saturation.setValue(color.getSaturation());
                lightness.setValue(color.getLightness());
                tintDisabled = false;
            }

            public BookSlider buildSlider(int x, int y, Component prefix, Component suffix, double currentVal, Consumer<Double> onValueChange) {
                return new BookSlider(x, y, 100, 20, prefix, suffix, 1.0D, 255.0D, currentVal, 1, 1, false, onValueChange);
            }

            @Override
            public void renderIcon(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
                if(!isLegacyRGB && tintDisabled){
                    DocClientUtils.blit(graphics, DocAssets.STYLE_ICON_NONE, x, y );
                    return;
                }
                Color color = new Color(displayColor.getColor(), false);
                graphics.fill(x + 2, y + 2, x + 12,  y + 12, color.getRGB());
            }

            @Override
            public Component getButtonTitle() {
                return getName();
            }
        };
    }

    @Override
    public IPropertyType<ColorProperty> getType() {
        return ParticlePropertyRegistry.COLOR_PROPERTY.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ColorProperty property = (ColorProperty) o;
        return Objects.equals(particleColor, property.particleColor) && tintDisabled == property.tintDisabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(particleColor, tintDisabled);
    }
}
