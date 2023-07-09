package dev.andante.bubble.world

import dev.andante.bubble.world.property.DefaultedLevelProperties
import dev.andante.bubble.world.property.EmptyWorldGenerationProgressListener
import net.minecraft.registry.RegistryKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ProgressListener
import net.minecraft.util.Util
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionOptions

/**
 * A bubble world. Inherently temporary, not saved to disk.
 */
open class BubbleWorld(server: MinecraftServer, worldRegistryKey: RegistryKey<World>, options: DimensionOptions) :
    ServerWorld(
        server,
        Util.getMainWorkerExecutor(),
        server.session,
        DefaultedLevelProperties(server.saveProperties),
        worldRegistryKey,
        options,
        EmptyWorldGenerationProgressListener,
        false,
        0,
        emptyList(),
        false,
        null
    ), IBubbleWorld {
    override val worldDimensionOptions: DimensionOptions = options

    /**
     * Disables most saving of the world.
     */
    override fun save(listener: ProgressListener?, flush: Boolean, savingDisabled: Boolean) {
    }

    override fun getWorldPlayers(): MutableList<ServerPlayerEntity> {
        return players
    }

    override fun asServerWorld(): ServerWorld {
        return this
    }
}
