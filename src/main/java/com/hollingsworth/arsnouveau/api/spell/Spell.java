package com.hollingsworth.arsnouveau.api.spell;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParser;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.particle.timelines.TimelineMap;
import com.hollingsworth.arsnouveau.api.sound.ConfiguredSpellSound;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.*;

public class Spell {

    public static final MapCodec<Spell> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(s -> s.name),
            ParticleColor.CODEC.fieldOf("color").forGetter(s -> s.color),
            ConfiguredSpellSound.CODEC.fieldOf("sound").forGetter(s -> s.sound),
            Codec.list(AbstractSpellPart.CODEC).fieldOf("recipe").forGetter(s -> s.recipe),
            TimelineMap.CODEC.optionalFieldOf("particleTimeline").forGetter(s -> Optional.ofNullable(s.particleTimeline))
    ).apply(instance, Spell::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Spell> STREAM = StreamCodec.of(
            (buf, val) -> {
                buf.writeUtf(val.name);
                ParticleColor.STREAM.encode(buf, val.color);
                ConfiguredSpellSound.STREAM.encode(buf, val.sound);
                AbstractSpellPart.STREAM_LIST.encode(buf, val.recipe);
                TimelineMap.STREAM.encode(buf, val.particleTimeline);
            },
            buf -> {
                String name = buf.readUtf();
                ParticleColor color = ParticleColor.STREAM.decode(buf);
                ConfiguredSpellSound sound = ConfiguredSpellSound.STREAM.decode(buf);
                List<AbstractSpellPart> recipe = AbstractSpellPart.STREAM_LIST.decode(buf);
                TimelineMap particleTimeline = TimelineMap.STREAM.decode(buf);
                return new Spell(name, color, sound, recipe, particleTimeline);
            }
    );


    private final List<AbstractSpellPart> recipe;
    private final String name;
    private final ParticleColor color;
    private final ConfiguredSpellSound sound;
    private final TimelineMap particleTimeline;


    public Spell() {
        this("", ParticleColor.defaultParticleColor(), ConfiguredSpellSound.DEFAULT, ImmutableList.of(), new TimelineMap());
    }

    public Spell(AbstractSpellPart... spellParts) {
        this(Arrays.asList(spellParts));
    }

    public Spell(List<AbstractSpellPart> recipe, String name) {
        this(name, ParticleColor.defaultParticleColor(), ConfiguredSpellSound.DEFAULT, recipe);
    }

    public Spell(List<AbstractSpellPart> recipe) {
        this("", ParticleColor.defaultParticleColor(), ConfiguredSpellSound.DEFAULT, recipe, new TimelineMap());
    }

    @Deprecated(forRemoval = true)
    public Spell(String name, ParticleColor color, ConfiguredSpellSound configuredSpellSound, List<AbstractSpellPart> abstractSpellParts) {
        this(name, color, configuredSpellSound, abstractSpellParts, new TimelineMap());
    }

    public Spell(String name, ParticleColor color, ConfiguredSpellSound configuredSpellSound, List<AbstractSpellPart> abstractSpellParts, TimelineMap particleTimeline) {
        this.name = name;
        this.color = color;
        this.sound = configuredSpellSound;
        this.recipe = ImmutableList.copyOf(abstractSpellParts);
        this.particleTimeline = particleTimeline;
    }

    public Spell(String name, ParticleColor color, ConfiguredSpellSound configuredSpellSound, List<AbstractSpellPart> abstractSpellParts, Optional<TimelineMap> particleTimeline) {
        this(name, color, configuredSpellSound, abstractSpellParts, particleTimeline.orElseGet(TimelineMap::new));
    }


    public static Spell fromJson(String jsonString) {

        try {
            Spell spell = CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(jsonString)).getOrThrow();
            System.out.println("About to read full spell from JSON: " + jsonString);
            System.out.println("Full decoded spell: " + spell);
            return spell;
        } catch (Exception e) {
            System.out.println("Failed to read spell from JSON: " + e.getMessage());
        }

        return new Spell();

    }

    public static Spell fromBinaryBase64(String base64) {

//        try{
//            byte[] data = Base64.getDecoder().decode(base64);
//            FriendlyByteBuf baseBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
//            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(baseBuf, ArsNouveau.proxy.getPlayer().registryAccess(), ConnectionType.NEOFORGE);
//
//            Spell spell = Spell.STREAM.decode(buf);
//
//            System.out.println("About to read full spell from binary base64: " + base64);
//            System.out.println("Full decoded spell: " + spell);
//            return spell;
//        }catch (Exception e) {
//            System.out.println("Failed to read spell from binary base64: " + e.getMessage());
//        }

        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

            int version = in.readByte();
            if (version != 2) {
                throw new IllegalArgumentException("Unsupported spell version: " + version);
            }

            System.out.println("About to read spell from binary base64: " + base64);

            String name = in.readUTF();
            System.out.println("Spell name: " + name);
            int partCount = in.readInt();
            System.out.println("Part count: " + partCount);
            List<AbstractSpellPart> recipe = new ArrayList<>();

            for (int i = 0; i < partCount; i++) {
                String partId = in.readUTF();
                System.out.println("Part ID: " + partId);
                AbstractSpellPart part = GlyphRegistry.getSpellpartMap()
                        .getOrDefault(ResourceLocation.tryParse(partId), null);
                if (part != null) {
                    recipe.add(part);
                }
            }

            return new Spell(name, ParticleColor.defaultParticleColor(), ConfiguredSpellSound.DEFAULT, recipe);

        } catch (IOException | NullPointerException e) {
            System.out.println("Failed to read spell from binary base64: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("String format not valid.");
        }
        return new Spell();
    }


    @Deprecated(forRemoval = true)
    public ConfiguredSpellSound sound() {
        return sound;
    }

    public Iterable<AbstractSpellPart> recipe() {
        return recipe;
    }

    /**
     * DO NOT MUTATE.
     * See {@link Spell#mutable()} for a mutable version.
     */
    public List<AbstractSpellPart> unsafeList() {
        return recipe;
    }

    public AbstractSpellPart get(int index) {
        return recipe.get(index);
    }

    public int size() {
        return recipe.size();
    }

    public int indexOf(AbstractSpellPart part) {
        return recipe.indexOf(part);
    }

    public Spell add(AbstractSpellPart spellPart) {
        return new Spell(name, color, sound, Util.copyAndAdd(recipe, spellPart), particleTimeline);
    }

    public Spell add(AbstractSpellPart... spellParts) {
        var spell = this;
        for (AbstractSpellPart part : spellParts)
            spell = spell.add(part);
        return spell;
    }

    public Spell add(AbstractSpellPart spellPart, int count) {
        var spell = this;
        for (int i = 0; i < count; i++) {
            spell = spell.add(spellPart);
        }
        return spell;
    }

    public Spell setRecipe(@NotNull List<AbstractSpellPart> recipe) {
        return new Spell(name, color, sound, ImmutableList.copyOf(recipe), particleTimeline);
    }

    public Spell withColor(@NotNull ParticleColor color) {
        return new Spell(name, color, sound, recipe, particleTimeline);
    }

    public Spell withSound(@NotNull ConfiguredSpellSound sound) {
        return new Spell(name, color, sound, recipe, particleTimeline);
    }

    public Spell withTimeline(@NotNull TimelineMap timeline) {
        return new Spell(name, color, sound, recipe, timeline);
    }

    public Spell withName(String name) {
        return new Spell(name, color, sound, recipe, particleTimeline);
    }

    @Deprecated(forRemoval = true)
    public ParticleColor color() {
        return color;
    }

    public String name() {
        return name;
    }


    public TimelineMap particleTimeline() {
        return particleTimeline;
    }

    public @Nullable AbstractCastMethod getCastMethod() {
        if (this.recipe == null || this.recipe.isEmpty())
            return null;
        return this.recipe.getFirst() instanceof AbstractCastMethod ? (AbstractCastMethod) recipe.getFirst() : null;
    }

    public List<AbstractAugment> getAugments(int startPosition, @Nullable LivingEntity caster) {
        ArrayList<AbstractAugment> augments = new ArrayList<>();
        if (recipe == null || recipe.isEmpty())
            return augments;
        for (int j = startPosition + 1; j < recipe.size(); j++) {
            AbstractSpellPart nextGlyph = recipe.get(j);
            if (nextGlyph instanceof AbstractAugment augment) {
                augments.add(augment);
            } else {
                break;
            }
        }

        return augments;
    }

    public int getInstanceCount(AbstractSpellPart spellPart) {
        int count = 0;
        for (AbstractSpellPart abstractSpellPart : this.recipe) {
            if (abstractSpellPart.equals(spellPart))
                count++;
        }
        return count;
    }

    public int getBuffsAtIndex(int startPosition, @Nullable LivingEntity caster, AbstractAugment augment) {
        return (int) getAugments(startPosition, caster).stream().filter(a -> a.equals(augment)).count();
    }

    public int getCost() {
        int cost = 0;
        AbstractSpellPart augmentedPart = null;
        for (AbstractSpellPart part : recipe) {
            if (part == null)
                continue;
            if (!(part instanceof AbstractAugment))
                augmentedPart = part;

            if (augmentedPart != null && part instanceof AbstractAugment augment) {
                cost += augment.getCostForPart(augmentedPart);
            } else {
                cost += part.getCastingCost();
            }
        }
        return cost;
    }

    public boolean isEmpty() {
        return recipe == null || recipe.isEmpty();
    }

    public String getDisplayString() {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < recipe.size(); i++) {
            AbstractSpellPart spellPart = recipe.get(i);
            int num = 1;
            for (int j = i + 1; j < recipe.size(); j++) {
                if (spellPart.name.equals(recipe.get(j).name))
                    num++;
                else
                    break;
            }
            if (num > 1) {
                str.append(spellPart.getLocaleName()).append(" x").append(num);
                i += num - 1;
            } else {
                str.append(spellPart.getLocaleName());
            }
            if (i < recipe.size() - 1) {
                str.append(" -> ");
            }
        }
        return str.toString();
    }

    public boolean isValid() {
        return !this.isEmpty();
    }

    /**
     * @deprecated Use {@link Mutable#add(AbstractSpellPart, int, int)} or {@link Mutable#add(AbstractSpellPart)} instead.
     * This method will be removed in a future version as it's backed by an immutable list.
     */
    @Deprecated(forRemoval = true)
    public Spell add(AbstractSpellPart spellPart, int count, int index) {
        for (int i = 0; i < count; i++)
            recipe.add(index, spellPart);
        return this;
    }

    public List<ResourceLocation> serializeRecipe() {
        return this.recipe.stream().map(AbstractSpellPart::getRegistryName).toList();
    }

    public Mutable mutable() {
        return new Mutable(new ArrayList<>(recipe), name, color, sound, particleTimeline);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spell spell = (Spell) o;
        return Objects.equals(recipe, spell.recipe) && Objects.equals(name, spell.name) && Objects.equals(color, spell.color) && Objects.equals(sound, spell.sound) && Objects.equals(particleTimeline, spell.particleTimeline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, name, color, sound, particleTimeline);
    }

    public static class Mutable {
        public List<AbstractSpellPart> recipe;
        public String name;
        public ParticleColor color;
        public ConfiguredSpellSound sound;
        public TimelineMap particleTimeline;

        public Mutable(List<AbstractSpellPart> recipe, String name, ParticleColor color, ConfiguredSpellSound spellSound, TimelineMap timeline) {
            this.recipe = recipe;
            this.name = name;
            this.color = color;
            this.sound = spellSound;
            this.particleTimeline = timeline;
        }

        public Mutable(List<AbstractSpellPart> recipe, String name, ParticleColor color, ConfiguredSpellSound spellSound) {
            this(recipe, name, color, spellSound, new TimelineMap());
        }

        public Mutable add(AbstractSpellPart spellPart) {
            recipe.add(spellPart);
            return this;
        }

        public Mutable add(AbstractSpellPart... spellParts) {
            recipe.addAll(Arrays.asList(spellParts));
            return this;
        }

        public Mutable add(int index, AbstractSpellPart spellPart) {
            recipe.add(index, spellPart);
            return this;
        }

        public Mutable add(AbstractSpellPart spellPart, int count, int index) {
            for (int i = 0; i < count; i++)
                recipe.add(index, spellPart);
            return this;
        }

        public Mutable setRecipe(@NotNull List<AbstractSpellPart> recipe) {
            this.recipe = recipe;
            return this;
        }


        public Spell immutable() {
            return new Spell(name, color, sound, recipe, particleTimeline);
        }
    }
}
