package dev.andante.bubble.world

import net.minecraft.registry.RegistryKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionOptions

/**
 * A factory for creating an instance of [BubbleWorld].
 */
fun interface BubbleWorldFactory<T : IBubbleWorld> {
    /**
     * Creates a bubble world.
     */
    fun create(server: MinecraftServer, worldRegistryKey: RegistryKey<World>, options: DimensionOptions): T
}
