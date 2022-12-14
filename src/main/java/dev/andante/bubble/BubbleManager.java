package dev.andante.bubble;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Lifecycle;
import dev.andante.bubble.mixin.MinecraftServerAccessor;
import dev.andante.bubble.util.DimensionUtil;
import dev.andante.bubble.util.RemovableSimpleRegistry;
import dev.andante.bubble.world.BubbleDimensionOptionsAccess;
import dev.andante.bubble.world.TemporaryWorld;
import dev.andante.bubble.world.VoidChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class BubbleManager {
    private static final Function<MinecraftServer, BubbleManager> MANAGERS = Util.memoize(BubbleManager::new);

    private final MinecraftServer server;
    private final Set<TemporaryWorld> worldsToDelete;

    private BubbleManager(MinecraftServer server) {
        this.server = server;
        this.worldsToDelete = new HashSet<>();
    }

    /**
     * Gets an instance of {@link BubbleManager} relevant to the given {@link MinecraftServer},
     * or creates one if one has not been created yet.
     */
    public static BubbleManager getOrCreate(MinecraftServer server) {
        Preconditions.checkState(server.isOnThread(), "Cannot create manager off-thread");
        return MANAGERS.apply(server);
    }

    public TemporaryWorld createAndInitialize(ChunkGenerator chunkGenerator) {
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, this.generateTemporaryWorldKey());

        // mark world for removal on close
        try {
            LevelStorage.Session session = ((MinecraftServerAccessor) this.server).getSession();
            FileUtils.forceDeleteOnExit(session.getWorldDirectory(key).toFile());
        } catch (IOException ignored) {
        }

        // add and return
        return this.add(key, chunkGenerator);
    }

    public TemporaryWorld createAndInitialize() {
        Registry<Biome> registry = this.server.getRegistryManager().get(RegistryKeys.BIOME);
        return this.createAndInitialize(new VoidChunkGenerator(registry));
    }

    public void scheduleDelete(TemporaryWorld world) {
        this.server.submit(() -> this.worldsToDelete.add(world));
    }

    public Identifier generateTemporaryWorldKey() {
        return new Identifier(Bubble.MOD_ID, "temp_world_" + UUID.randomUUID());
    }

    public static void tick(MinecraftServer server) {
        BubbleManager.getOrCreate(server).tick();
    }

    public static void onServerStopping(MinecraftServer server) {
        BubbleManager.getOrCreate(server).onServerStopping();
    }

    protected void tick() {
        if (!this.worldsToDelete.isEmpty()) {
            this.worldsToDelete.removeIf(this::tryDelete);
        }
    }

    public boolean isWorldUnloaded(ServerWorld world) {
        return world.getPlayers().isEmpty() && world.getChunkManager().getLoadedChunkCount() <= 0;
    }

    protected void onServerStopping() {
        List<TemporaryWorld> worlds = this.collectAll();
        for (TemporaryWorld world : worlds) {
            this.kickPlayers(world);
            this.delete(world);
        }
    }

    private List<TemporaryWorld> collectAll() {
        return StreamSupport.stream(this.server.getWorlds().spliterator(), false)
                            .filter(world -> world instanceof TemporaryWorld)
                            .map(world -> (TemporaryWorld) world)
                            .toList();
    }

    @SuppressWarnings("unchecked")
    protected TemporaryWorld add(RegistryKey<World> key, ChunkGenerator chunkGenerator) {
        // create options
        DimensionOptions options = DimensionUtil.createDimensionOptions(this.server, Bubble.DEFAULT_DIMENSION_TYPE, chunkGenerator);
        ((BubbleDimensionOptionsAccess) (Object) options).setShouldSave(false);

        // registry stuff
        SimpleRegistry<DimensionOptions> dimensionsRegistry = this.getDimensionsRegistry(this.server);
        RemovableSimpleRegistry<DimensionOptions> removable = (RemovableSimpleRegistry<DimensionOptions>) dimensionsRegistry;
        boolean wasFrozen = removable.isFrozen();
        removable.setFrozen(false);
        dimensionsRegistry.add(RegistryKey.of(RegistryKeys.DIMENSION, key.getValue()), options, Lifecycle.stable());
        removable.setFrozen(wasFrozen);

        // create world
        TemporaryWorld world = new TemporaryWorld(this.server, key, options);

        // add to server worlds
        Map<RegistryKey<World>, ServerWorld> worlds = ((MinecraftServerAccessor) this.server).getWorlds();
        worlds.put(key, world);

        // invoke fabric event
        ServerWorldEvents.LOAD.invoker().onWorldLoad(this.server, world);

        // pop a tick on the world
        world.tick(() -> true);

        return world;
    }

    protected void delete(TemporaryWorld world) {
        RegistryKey<World> key = world.getRegistryKey();
        MinecraftServerAccessor accessor = (MinecraftServerAccessor) this.server;
        if (accessor.getWorlds().remove(key, world)) {
            // call fabric event
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this.server, world);

            // remove from registry
            SimpleRegistry<DimensionOptions> registry = getDimensionsRegistry(this.server);
            RemovableSimpleRegistry.remove(registry, key);

            // delete directory
            LevelStorage.Session session = accessor.getSession();
            File directory = session.getWorldDirectory(key).toFile();
            if (directory.exists()) {
                try {
                    FileUtils.deleteDirectory(directory);
                } catch (IOException exception) {
                    Bubble.LOGGER.warn("Failed to delete world directory", exception);
                    try {
                        FileUtils.forceDeleteOnExit(directory);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    protected boolean tryDelete(TemporaryWorld world) {
        if (this.isWorldUnloaded(world)) {
            this.delete(world);
            return true;
        } else {
            this.kickPlayers(world);
        }

        return false;
    }

    protected void kickPlayers(TemporaryWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        ServerWorld overworld = this.server.getOverworld();
        BlockPos pos = overworld.getSpawnPos();
        float angle = overworld.getSpawnAngle();
        for (ServerPlayerEntity player : new ArrayList<>(players)) {
            player.teleport(overworld, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, angle, 0.0F);
        }
    }

    protected SimpleRegistry<DimensionOptions> getDimensionsRegistry(MinecraftServer server) {
        DynamicRegistryManager registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();
        return (SimpleRegistry<DimensionOptions>) registryManager.get(RegistryKeys.DIMENSION);
    }
}
