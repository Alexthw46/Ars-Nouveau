package com.hollingsworth.arsnouveau.common.items.itemscrolls;

import com.hollingsworth.arsnouveau.common.items.ItemScroll;
import com.hollingsworth.arsnouveau.common.items.data.ItemScrollData;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class DenyItemScroll extends ItemScroll {

    public DenyItemScroll() {
        super();
    }

    public DenyItemScroll(Properties properties) {
        super(properties);
    }

    @Override
    public SortPref getSortPref(ItemStack stackToStore, ItemStack scrollStack, IItemHandler inventory) {
        ItemScrollData data = scrollStack.getOrDefault(DataComponentRegistry.ITEM_SCROLL_DATA, new ItemScrollData());
        return !data.containsStack(stackToStore) ? SortPref.HIGH : SortPref.INVALID;
    }
}
