package dev.andante.bubble

import com.google.common.base.Preconditions
import com.mojang.serialization.Lifecycle
import dev.andante.bubble.registry.RemovableSimpleRegistry
import dev.andante.bubble.world.BubbleWorld
import dev.andante.bubble.world.BubbleWorldFactory
import dev.andante.bubble.world.IBubbleWorld
import dev.andante.bubble.world.property.VoidChunkGenerator
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.SimpleRegistry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionOptions
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.ChunkGenerator
import org.apache.commons.io.FileUtils
import java.io.IOException
import java.util.UUID

/**
 * Manages Bubble worlds for the given server.
 */
class BubbleManager private constructor(private val server: MinecraftServer) {
    /**
     * A running queue of the worlds that need to be deleted.
     */
    private val worldsToDelete = mutableSetOf<IBubbleWorld>()

    /**
     * Creates a bubble world from the given parameters and initializes it.
     */
    fun <T : IBubbleWorld> createAndInitialize(
        /**
         * The chunk generator to use.
         */
        chunkGenerator: ChunkGenerator = run {
            val registry = server.registryManager[RegistryKeys.BIOME]
            VoidChunkGenerator(registry)
        },

        /**
         * The unique identifier of the world.
         */
        identifier: Identifier = run {
            val uuid = UUID.randomUUID()
            Identifier(Bubble.MOD_ID, "temp_world_$uuid")
        },

        /**
         * The factory to create the bubble world.
         */
        factory: BubbleWorldFactory<T>
    ): T {
        val key = RegistryKey.of(RegistryKeys.WORLD, identifier)

        // mark world for file deletion on close
        try {
            val session = server.session
            FileUtils.forceDeleteOnExit(session.getWorldDirectory(key).toFile())
        } catch (ignored: IOException) {
        }

        // add and return
        val world = create(key, chunkGenerator, factory)
        return register(world)
    }

    /**
     * Creates a bubble world with the default parameters.
     */
    fun createAndInitialize(): BubbleWorld {
        return createAndInitialize(factory = ::BubbleWorld)
    }

    /**
     * Schedules the given bubble world for removal.
     */
    fun remove(world: IBubbleWorld) {
        server.submit { worldsToDelete.add(world) }
    }

    /**
     * Ticks the bubble manager.
     */
    private fun tick() {
        // try delete worlds
        worldsToDelete.removeAll(worldsToDelete.filter(::tryDelete).toSet())
    }

    /**
     * Kicks players from all bubble worlds and deletes the bubble world.
     */
    private fun clean() {
        collectAllWorlds().forEach { world ->
            kickPlayers(world)
            delete(world)
        }
    }

    /**
     * @return all bubble server worlds from the server
     */
    private fun collectAllWorlds(): List<IBubbleWorld> {
        return server.getWorlds()
            .filterIsInstance<IBubbleWorld>()
    }

    /**
     * Creates a bubble world from the given parameters.
     */
    private fun <T : IBubbleWorld> create(
        key: RegistryKey<World>,
        chunkGenerator: ChunkGenerator,
        factory: BubbleWorldFactory<T>
    ): T {
        val options = DimensionOptions(getRegistryEntry(server, Bubble.DEFAULT_DIMENSION_TYPE), chunkGenerator)
        return factory.create(server, key, options)
    }

    /**
     * Registers the given bubble world.
     */
    private fun <T : IBubbleWorld> register(world: T): T {
        val serverWorld = world.asServerWorld()
        val key = serverWorld.registryKey

        // registry stuff
        val dimensionsRegistry = getDimensionsRegistry(server)
        val removable = dimensionsRegistry as RemovableSimpleRegistry<*>
        val wasFrozen = removable.isFrozen
        removable.isFrozen = false
        dimensionsRegistry.add(RegistryKey.of(RegistryKeys.DIMENSION, key.value), world.worldDimensionOptions, Lifecycle.stable())
        removable.isFrozen = wasFrozen

        // add to server worlds
        val worlds = server.worlds
        worlds[key] = serverWorld

        // invoke fabric event
        ServerWorldEvents.LOAD.invoker().onWorldLoad(server, serverWorld)

        // pop a tick on the world
        serverWorld.tick { true }

        return world
    }

    /**
     * @return the entry of the given [type] in the server's registries, or throw
     */
    private fun getRegistryEntry(
        server: MinecraftServer,
        type: RegistryKey<DimensionType>
    ): RegistryEntry<DimensionType> {
        return server.registryManager
            .get(RegistryKeys.DIMENSION_TYPE)
            .getEntry(type)
            .orElseThrow { IllegalArgumentException("Could not fetch registry entry") }
    }

    /**
     * Deletes the given bubble world.
     */
    private fun delete(world: IBubbleWorld) {
        val serverWorld = world.asServerWorld()
        val key = world.worldRegistryKey
        if (server.worlds.remove(key, serverWorld)) {
            // call fabric event
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(server, serverWorld)

            // remove from registry
            val registry = getDimensionsRegistry(server)
            RemovableSimpleRegistry.remove(registry, key)

            // delete directory
            val session = server.session
            val directory = session.getWorldDirectory(key).toFile()
            if (directory.exists()) {
                try {
                    FileUtils.deleteDirectory(directory)
                } catch (exception: IOException) {
                    Bubble.LOGGER.warn("Failed to delete world directory", exception)
                    try {
                        FileUtils.forceDeleteOnExit(directory)
                    } catch (ignored: IOException) {
                    }
                }
            }
        }
    }

    /**
     * Tries to delete the given world. Only succeeds if the world is unloaded.
     */
    private fun tryDelete(world: IBubbleWorld): Boolean {
        return if (isWorldUnloaded(world.asServerWorld())) {
            delete(world)
            true
        } else {
            kickPlayers(world)
            false
        }
    }

    /**
     * @return whether the world is unloaded
     */
    private fun isWorldUnloaded(world: ServerWorld): Boolean {
        return world.players.isEmpty() && world.chunkManager.loadedChunkCount <= 0
    }

    /**
     * Kicks all players back to the overworld spawn position.
     */
    private fun kickPlayers(world: IBubbleWorld) {
        val players = world.getWorldPlayers()
        if (players.isEmpty()) {
            return
        }

        val overworld = server.overworld
        val spawnPos = overworld.spawnPos
        val angle = overworld.spawnAngle
        val pos = Vec3d.ofBottomCenter(spawnPos)
        players.forEach { player -> player.teleport(overworld, pos.x, pos.y, pos.z, angle, 0.0f) }
    }

    /**
     * Retrieves the dimensions registry from the given [server].
     */
    private fun getDimensionsRegistry(server: MinecraftServer): SimpleRegistry<DimensionOptions> {
        val registryManager = server.combinedDynamicRegistries.combinedRegistryManager
        return registryManager.get(RegistryKeys.DIMENSION) as SimpleRegistry<DimensionOptions>
    }

    companion object {
        /**
         * All bubble world managers.
         */
        private val MANAGERS = mutableMapOf<MinecraftServer, BubbleManager>()

        init {
            // register server events
            ServerTickEvents.START_SERVER_TICK.register { MANAGERS[it]?.tick() }
            ServerLifecycleEvents.SERVER_STOPPING.register { MANAGERS[it]?.clean() }
        }

        /**
         * Gets an instance of [BubbleManager] relevant to the given [server],
         * or creates one if one has not been created yet.
         */
        fun getOrCreate(server: MinecraftServer): BubbleManager {
            Preconditions.checkState(server.isOnThread, "Cannot create manager off-thread")
            return MANAGERS.computeIfAbsent(server, ::BubbleManager)
        }

        /**
         * Clears the instance of [BubbleManager] for the given [server].
         */
        fun clear(server: MinecraftServer): BubbleManager? {
            MANAGERS[server]?.let(BubbleManager::clean)
            return MANAGERS.remove(server)
        }
    }
}
