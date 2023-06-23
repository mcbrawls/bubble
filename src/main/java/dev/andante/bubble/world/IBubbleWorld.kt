package dev.andante.bubble.world

import net.minecraft.registry.RegistryKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionOptions

/**
 * An abstracted bubble world.
 */
interface IBubbleWorld {
    /**
     * The world registry key.
     */
    val worldRegistryKey: RegistryKey<World> get() = asServerWorld().registryKey

    /**
     * The world's dimension options.
     */
    val worldDimensionOptions: DimensionOptions

    /**
     * Retrieves all the players of the world.
     */
    fun getWorldPlayers(): List<ServerPlayerEntity>

    /**
     * This bubble world represented as a server world.
     */
    fun asServerWorld(): ServerWorld
}
