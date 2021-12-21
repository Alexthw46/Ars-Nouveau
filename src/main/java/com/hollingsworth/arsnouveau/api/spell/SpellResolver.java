package com.hollingsworth.arsnouveau.api.spell;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellResolveEvent;
import com.hollingsworth.arsnouveau.api.util.SpellUtil;
import com.hollingsworth.arsnouveau.common.capability.CapabilityRegistry;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hollingsworth.arsnouveau.api.util.ManaUtil.getPlayerDiscounts;

public class SpellResolver {
    public AbstractCastMethod castType;
    public Spell spell;
    public final SpellContext spellContext;
    public boolean silent;
    private final ISpellValidator spellValidator;

    public SpellResolver(SpellContext spellContext){
        this.spell = spellContext.getSpell();
        this.castType = spellContext.getSpell().getCastMethod();
        this.spellContext = spellContext;
        this.spellValidator = ArsNouveauAPI.getInstance().getSpellCastingSpellValidator();
    }

    public SpellResolver withSilent(boolean isSilent){
        this.silent = isSilent;
        return this;
    }

    public boolean canCast(LivingEntity entity){
        // Validate the spell
        List<SpellValidationError> validationErrors = spellValidator.validate(spell.recipe);

        if (validationErrors.isEmpty()) {
            // Validation successful. We can check the player's mana now.
            return enoughMana(entity);
        } else {
            // Validation failed, explain why if applicable
            if (!silent && !entity.getCommandSenderWorld().isClientSide) {
                // Sending only the first error to avoid spam
                PortUtil.sendMessageNoSpam(entity, validationErrors.get(0).makeTextComponentExisting());
            }
            return false;
        }
    }

    boolean enoughMana(LivingEntity entity){
        int totalCost = getCastingCost(spell, entity);
        AtomicBoolean canCast = new AtomicBoolean(false);
        CapabilityRegistry.getMana(entity).ifPresent(mana -> {
            canCast.set(totalCost <= mana.getCurrentMana() || (entity instanceof Player &&  ((Player) entity).isCreative()));
            if(!canCast.get() && !entity.getCommandSenderWorld().isClientSide && !silent)
                PortUtil.sendMessageNoSpam(entity,new TranslatableComponent("ars_nouveau.spell.no_mana"));
        });
        return canCast.get();
    }

    public boolean postEvent(LivingEntity entity){
        return SpellUtil.postEvent(new SpellCastEvent(entity, spell, spellContext));
    }

    public void onCast(ItemStack stack, LivingEntity livingEntity, Level world){
        if(canCast(livingEntity) && !postEvent(livingEntity)) {
            SpellStats stats = new SpellStats.Builder()
                    .setAugments(spell.getAugments(0, livingEntity))
                    .addItemsFromEntity(livingEntity)
                    .build(castType, null, world, livingEntity, spellContext);
            castType.onCast(stack, livingEntity, world, stats, spellContext, this);
        }
    }

    private SpellStats getCastStats(LivingEntity caster, @Nullable HitResult result){
        return new SpellStats.Builder()
                .setAugments(spell.getAugments(0, caster))
                .addItemsFromEntity(caster)
                .build(castType, result, caster.level, caster, spellContext);
    }

    public void onCastOnBlock(BlockHitResult blockRayTraceResult, LivingEntity caster){
        if(canCast(caster) && !postEvent(caster))
            castType.onCastOnBlock(blockRayTraceResult, caster,getCastStats(caster, blockRayTraceResult), spellContext, this);
    }

    public void onCastOnBlock(UseOnContext context){
        if(canCast(context.getPlayer()) && !postEvent(context.getPlayer()))
            castType.onCastOnBlock(context,getCastStats(context.getPlayer(),context.hitResult), spellContext, this);
    }

    public void onCastOnEntity(ItemStack stack, LivingEntity playerIn, Entity target, InteractionHand hand){
        if(canCast(playerIn) && !postEvent(playerIn))
            castType.onCastOnEntity(stack, playerIn, target, hand, getCastStats(playerIn, new EntityHitResult(target)), spellContext, this);
    }

    public void onResolveEffect(Level world, LivingEntity shooter, HitResult result){
        SpellResolver.resolveEffects(world, shooter, result, spell, spellContext);
    }

    public static void resolveEffects(Level world, LivingEntity shooter, HitResult result, Spell spell, SpellContext spellContext){
        spellContext.resetCastCounter();
        shooter = getUnwrappedCaster(world, shooter, spellContext);
        SpellResolveEvent.Pre spellResolveEvent = new SpellResolveEvent.Pre(world, shooter, result, spell, spellContext);
        MinecraftForge.EVENT_BUS.post(spellResolveEvent);
        if(spellResolveEvent.isCanceled())
            return;

        for(int i = 0; i < spell.recipe.size(); i++){
            if(spellContext.isCanceled())
                break;
            AbstractSpellPart part = spellContext.nextPart();
            if(part == null)
                return;
            SpellStats.Builder builder = new SpellStats.Builder();
            SpellStats stats = builder
                    .setAugments(spell.getAugments(i, shooter))
                    .addItemsFromEntity(shooter)
                    .build(part, result, world, shooter, spellContext);
            if(part instanceof AbstractEffect){
                ((AbstractEffect) part).onResolve(result, world, shooter, stats, spellContext);
            }
        }
        MinecraftForge.EVENT_BUS.post(new SpellResolveEvent.Post(world, shooter, result, spell, spellContext));
    }


    // Safely unwrap the living entity in the case that the caster is null, aka being cast by a non-player.
    public static LivingEntity getUnwrappedCaster(Level world, LivingEntity shooter, SpellContext spellContext){
        if(shooter == null && spellContext.castingTile != null) {
            shooter = FakePlayerFactory.getMinecraft((ServerLevel) world);
            BlockPos pos = spellContext.castingTile.getBlockPos();
            shooter.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
        shooter = shooter == null ? FakePlayerFactory.getMinecraft((ServerLevel) world) : shooter;
        return shooter;
    }

    public boolean wouldAllEffectsDoWork(HitResult result, Level world, LivingEntity entity,  SpellStats stats){
        for(AbstractSpellPart spellPart : spell.recipe){
            if(spellPart instanceof AbstractEffect){
                if(!((AbstractEffect) spellPart).wouldSucceed(result, world, entity, stats, spellContext)){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean wouldCastSuccessfully(@Nullable ItemStack stack, LivingEntity caster, Level world, SpellStats stats){
        return castType.wouldCastSuccessfully(stack, caster, world, stats, this);
    }

    public boolean wouldCastOnBlockSuccessfully(UseOnContext context,  SpellStats stats){
        return castType.wouldCastOnBlockSuccessfully(context, stats,this );
    }

    public boolean wouldCastOnBlockSuccessfully(BlockHitResult blockRayTraceResult, LivingEntity caster){
        return castType.wouldCastOnBlockSuccessfully(blockRayTraceResult, caster,  getCastStats(caster, blockRayTraceResult), this);
    }

    public boolean wouldCastOnEntitySuccessfully(@Nullable ItemStack stack, LivingEntity caster, LivingEntity target, InteractionHand hand,  SpellStats stats){
        return castType.wouldCastOnEntitySuccessfully(stack, caster, target, hand, stats,this);
    }


    public void expendMana(LivingEntity entity){
        int totalCost = getCastingCost(spell, entity);
        CapabilityRegistry.getMana(entity).ifPresent(mana -> mana.removeMana(totalCost));
    }

    public int getCastingCost(Spell spell, LivingEntity e){
        int cost = spell.getCastingCost() - getPlayerDiscounts(e);
        return Math.max(cost, 0);
    }
}
