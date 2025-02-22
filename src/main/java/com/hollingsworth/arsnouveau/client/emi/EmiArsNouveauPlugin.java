package com.hollingsworth.arsnouveau.client.emi;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.registry.RitualRegistry;
import com.hollingsworth.arsnouveau.common.crafting.recipes.*;
import com.hollingsworth.arsnouveau.common.lib.RitualLib;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectCrush;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

@EmiEntrypoint
public class EmiArsNouveauPlugin implements EmiPlugin {
    public static final EmiStack ENCHANTING_APPARATUS = EmiStack.of(BlockRegistry.ENCHANTING_APP_BLOCK);
    public static final EmiRecipeCategory ENCHANTING_APPARATUS_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("enchanting_apparatus"), ENCHANTING_APPARATUS);
    public static final EmiRecipeCategory APPARATUS_ENCHANTING_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("apparatus_enchanting"), ENCHANTING_APPARATUS);
    public static final EmiRecipeCategory ARMOR_UPGRADE_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("armor_upgrade"), ENCHANTING_APPARATUS);

    public static final EmiStack IMBUEMENT_CHAMBER = EmiStack.of(BlockRegistry.IMBUEMENT_BLOCK);
    public static final EmiRecipeCategory IMBUEMENT_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("imbuement"), IMBUEMENT_CHAMBER);

    public static final EmiStack SCRIBES_TABLE = EmiStack.of(BlockRegistry.SCRIBES_BLOCK);
    public static final EmiRecipeCategory GLYPH_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("glyph_recipe"), SCRIBES_TABLE);

    public static final EmiStack AMETHYST_GOLEM_CHARM = EmiStack.of(ItemsRegistry.AMETHYST_GOLEM_CHARM);
    public static final EmiRecipeCategory BUDDING_CONVERSION_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("budding_conversion"), AMETHYST_GOLEM_CHARM);

    public static final EmiStack CRUSH_GLYPH = EmiStack.of(EffectCrush.INSTANCE.glyphItem);
    public static final EmiRecipeCategory CRUSH_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("crush"), CRUSH_GLYPH);

    public static final EmiStack SCRY_TABLET = EmiStack.of(RitualRegistry.getRitualItemMap().get(ArsNouveau.prefix(RitualLib.SCRYING)));
    public static final EmiRecipeCategory SCRY_RITUAL_CATEGORY = new EmiRecipeCategory(ArsNouveau.prefix("scry_ritual"), SCRY_TABLET);

    @Override
    public void register(EmiRegistry registry) {
        this.registerCategories(registry);
        this.registerRecipes(registry);
    }

    public void registerCategories(EmiRegistry registry) {
        registry.addCategory(ENCHANTING_APPARATUS_CATEGORY);
        registry.addWorkstation(ENCHANTING_APPARATUS_CATEGORY, ENCHANTING_APPARATUS);

        registry.addCategory(APPARATUS_ENCHANTING_CATEGORY);
        registry.addWorkstation(APPARATUS_ENCHANTING_CATEGORY, ENCHANTING_APPARATUS);

        registry.addCategory(ARMOR_UPGRADE_CATEGORY);
        registry.addWorkstation(ARMOR_UPGRADE_CATEGORY, ENCHANTING_APPARATUS);

        registry.addCategory(IMBUEMENT_CATEGORY);
        registry.addWorkstation(IMBUEMENT_CATEGORY, IMBUEMENT_CHAMBER);

        registry.addCategory(GLYPH_CATEGORY);
        registry.addWorkstation(GLYPH_CATEGORY, SCRIBES_TABLE);

        registry.addCategory(BUDDING_CONVERSION_CATEGORY);
        registry.addWorkstation(BUDDING_CONVERSION_CATEGORY, AMETHYST_GOLEM_CHARM);

        registry.addCategory(CRUSH_CATEGORY);
        registry.addWorkstation(CRUSH_CATEGORY, CRUSH_GLYPH);

        registry.addCategory(SCRY_RITUAL_CATEGORY);
        registry.addWorkstation(SCRY_RITUAL_CATEGORY, SCRY_TABLET);
    }

    public void registerRecipes(@NotNull EmiRegistry registry) {
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        for (RecipeHolder<?> i : manager.getRecipes().stream().toList()) {
            var id = i.id();
            var emiRecipe = switch (i.value()) {
                case GlyphRecipe glyphRecipe -> new EmiGlyphRecipe(id, glyphRecipe);
                case EnchantmentRecipe enchantmentRecipe -> new EmiApparatusEnchantingRecipe(id, enchantmentRecipe);
                case ArmorUpgradeRecipe upgradeRecipe -> new EmiArmorUpgradeRecipe(id, upgradeRecipe);
                case EnchantingApparatusRecipe enchantingApparatusRecipe when !enchantingApparatusRecipe.excludeJei() ->
                        new EmiEnchantingApparatusRecipe<>(id, enchantingApparatusRecipe);
                case CrushRecipe crushRecipe -> new EmiCrushRecipe(id, crushRecipe);
                case BuddingConversionRecipe buddingConversionRecipe -> new EmiBuddingConversionRecipe(id, buddingConversionRecipe);
                case ScryRitualRecipe scryRitualRecipe -> new EmiScryRitualRecipe(id, scryRitualRecipe);
                default -> null;
            };

            if (emiRecipe != null) {
                registry.addRecipe(emiRecipe);
            }
        }

        for (var recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeRegistry.IMBUEMENT_TYPE.get())) {
            registry.addRecipe(new EmiImbuementRecipe(recipe.id(), recipe.value()));
        }
    }
}