package dev.andante.bubble.world;

import dev.andante.bubble.mixin.MinecraftServerAccessor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

import java.util.Collections;

public class TemporaryWorld extends ServerWorld {
    public TemporaryWorld(MinecraftServer server, RegistryKey<World> worldRegistryKey, DimensionOptions options) {
        super(server, Util.getMainWorkerExecutor(), ((MinecraftServerAccessor) server).getSession(), new DefaultedLevelProperties(server.getSaveProperties()), worldRegistryKey, options, EmptyWorldGenerationProgressListener.INSTANCE, false, 0, Collections.emptyList(), false, null);
    }

    @Override
    public void save(ProgressListener listener, boolean flush, boolean savingDisabled) {
    }
}
