package net.mcdivisions.bubble.world;

import net.mcdivisions.bubble.mixin.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class TemporaryWorld extends ServerWorld {
    public TemporaryWorld(MinecraftServer server, RegistryKey<World> worldRegistryKey, DimensionOptions options) {
        super(server, Util.getMainWorkerExecutor(), ((MinecraftServerAccessor) server).getSession(), new DefaultedLevelProperties(server.getSaveProperties()), worldRegistryKey, options, EmptyWorldGenerationProgressListener.INSTANCE, false, 0, Collections.emptyList(), false);
    }

    @Override
    public void save(@Nullable ProgressListener listener, boolean flush, boolean enabled) {
        if (flush) {
            return;
        }

        super.save(listener, flush, enabled);
    }
}
