package com.hollingsworth.arsnouveau.common.network;

import com.hollingsworth.arsnouveau.common.items.SpellBook;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSetBookMode {

    public CompoundNBT tag;
    //Decoder
    public PacketSetBookMode(PacketBuffer buf){
        tag = buf.readCompoundTag();
    }

    //Encoder
    public void toBytes(PacketBuffer buf){
        buf.writeCompoundTag(tag);
    }

    public PacketSetBookMode(CompoundNBT tag){
        this.tag = tag;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            ctx.get().enqueueWork(()-> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null) return;

                ItemStack stack = sender.getHeldItemMainhand();
                if (stack.getItem() instanceof SpellBook) {
                    stack.setTag(tag);
                    return;
                }

                stack = sender.getHeldItemOffhand();
                if (stack.getItem() instanceof SpellBook) {
                    stack.setTag(tag);
                }
            });
        } );
        ctx.get().setPacketHandled(true);
    }
}
