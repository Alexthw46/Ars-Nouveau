package com.hollingsworth.arsnouveau.common.block;

import com.hollingsworth.arsnouveau.common.block.tile.MageBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MageBlock extends TickableModBlock {
    public static final BooleanProperty TEMPORARY = BooleanProperty.create("temporary");
    public static final IntegerProperty TRANSPARENCY = IntegerProperty.create("transparency", 0, 5);

    public MageBlock() {
        super(defaultProperties().lightLevel(bs -> 7).noOcclusion().dynamicShape());
        registerDefaultState(defaultBlockState().setValue(TEMPORARY, false).setValue(TRANSPARENCY, 0));
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new MageBlockTile(pos, state);
    }

    @Override
    public boolean canDropFromExplosion(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(TEMPORARY, TRANSPARENCY);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (state.hasProperty(TEMPORARY) && state.getValue(TEMPORARY)) ? super.getTicker(level, state, type) : null;
    }

    @Override
    protected boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        if (state.getValue(TRANSPARENCY) == 0) return super.skipRendering(state, adjacentBlockState, side);
        return adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, side);
    }

}

