package com.hollingsworth.arsnouveau.setup.registry;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.FireworkExplosion;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.FireworkShapeFactoryRegistry;
import org.jetbrains.annotations.NotNull;

public class FireworkShapes {

    public static final RandomSource random = RandomSource.createNewThreadLocalInstance();

    public static final EnumProxy<FireworkExplosion.Shape> STARBUNClE_SHAPE = new EnumProxy<>(
            FireworkExplosion.Shape.class, -1,
            "ars_nouveau:starbuncle"
    );
    private static final double[][] STARBUNCLE_COORDS = new double[][]{
            {-0.866667, 0.866667},
            {-0.733333, 0.866667},
            {-0.6, 0.866667},
            {-0.466667, 0.866667},
            {0.333333, 0.866667},
            {0.466667, 0.866667},
            {0.6, 0.866667},
            {0.733333, 0.866667},
            {-0.866667, 0.733333},
            {-0.333333, 0.733333},
            {0.2, 0.733333},
            {0.733333, 0.733333},
            {-0.866667, 0.6},
            {-0.333333, 0.6},
            {0.2, 0.6},
            {0.733333, 0.6},
            {-0.733333, 0.466667},
            {-0.333333, 0.466667},
            {0.2, 0.466667},
            {0.6, 0.466667},
            {-0.466667, 0.333333},
            {0.333333, 0.333333},
            {-0.333333, 0.2},
            {0.2, 0.2},
            {-0.466667, 0.066667},
            {-0.2, 0.066667},
            {0.066667, 0.066667},
            {0.333333, 0.066667},
            {-0.6, -0.066667},
            {0.466667, -0.066667},
            {-0.333333, -0.2},
            {0.2, -0.2},
            {-0.6, -0.333333},
            {-0.333333, -0.333333},
            {-0.066667, -0.333333},
            {0.2, -0.333333},
            {0.466667, -0.333333},
            {-0.6, -0.466667},
            {0.466667, -0.466667},
            {-0.466667, -0.6},
            {-0.2, -0.6},
            {0.066667, -0.6},
            {0.333333, -0.6},
    };

    public static void registerFireworkShapes() {
        FireworkShapeFactoryRegistry.register(STARBUNClE_SHAPE.getValue(), new FireworkShapeFactoryRegistry.Factory() {
            @Override
            public void build(FireworkParticles.@NotNull Starter starter, boolean trail, boolean twinkle, int[] colors, int[] fadeColors) {
                var coords = new double[][]{
                        {-1.0, 1.0},
                        {-0.875, 1.0},
                        {-0.75, 1.0},
                        {-0.625, 1.0},
                        {-0.5, 1.0},
                        {-0.375, 1.0},
                        {-1.0, 0.888889},
                        {-0.375, 0.888889},
                        {-1.0, 0.777778},
                        {-0.25, 0.777778},
                        {-0.125, 0.777778},
                        {-1.0, 0.666667},
                        {-0.125, 0.666667},
                        {-1.0, 0.555556},
                        {-0.125, 0.555556},
                        {-1.0, 0.444444},
                        {-0.875, 0.444444},
                        {-0.125, 0.444444},
                        {-0.75, 0.333333},
                        {-0.125, 0.333333},
                        {-0.75, 0.222222},
                        {-0.625, 0.222222},
                        {-0.5, 0.222222},
                        {-0.375, 0.222222},
                        {-0.125, 0.222222},
                        {-0.25, 0.111111},
                        {-0.125, 0.111111},
                        {-0.25, 0.0},
                        {-0.125, 0.0},
                        {-0.5, -0.111111},
                        {-0.375, -0.111111},
                        {-0.25, -0.111111},
                        {-0.125, -0.111111},
                        {0.0, -0.111111},
                        {-0.5, -0.222222},
                        {-0.5, -0.333333},
                        {-0.25, -0.333333},
                        {-0.5, -0.444444},
                        {-0.25, -0.444444},
                        {-0.5, -0.555556},
                        {-0.25, -0.555556},
                        {-0.5, -0.666667},
                        {0.0, -0.666667},
                        {-0.5, -0.777778},
                        {-0.5, -0.888889},
                        {-0.375, -0.888889},
                        {-0.25, -0.888889},
                        {-0.125, -0.888889},
                        {0.0, -0.888889},
                };
                starter.createParticleShape(
                        0.4,                             // speed
                        coords,          // your coords
                        IntList.of(colors),
                        IntList.of(fadeColors),
                        trail,
                        twinkle,
                        true                            // flat img
                );

            }
        });


    }

}
