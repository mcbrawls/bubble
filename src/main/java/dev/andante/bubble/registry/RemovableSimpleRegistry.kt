package dev.andante.bubble.registry

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier
import net.minecraft.world.World

/**
 * An extension of [SimpleRegistry] that supports removal.
 */
interface RemovableSimpleRegistry<T> {
    /**
     * Removes the given value from the registry.
     */
    fun remove(value: T): Boolean

    /**
     * Removes the given key from the registry.
     */
    fun remove(key: Identifier): Boolean

    /**
     * Whether the registry is currently frozen.
     */
    var isFrozen: Boolean

    companion object {
        /**
         * Removes the given [key] from [registry].
         */
        fun <T> remove(registry: SimpleRegistry<T>, key: RegistryKey<World>): Boolean {
            return (registry as RemovableSimpleRegistry<*>).remove(key.value)
        }
    }
}
