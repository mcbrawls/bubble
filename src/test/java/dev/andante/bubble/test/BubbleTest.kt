package dev.andante.bubble.test

import com.mojang.logging.LogUtils
import dev.andante.bubble.BubbleManager.Companion.getOrCreate
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.Logger

object BubbleTest : ModInitializer {
    private val LOGGER: Logger = LogUtils.getLogger()

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            val manager = getOrCreate(server)
            val deletedWorld = manager.createAndInitialize()
            LOGGER.info("{}", deletedWorld.getRegistryKey().value)
            val world = manager.createAndInitialize()
            LOGGER.info("{}", world.getRegistryKey().value)
            manager.remove(deletedWorld)
        }
    }
}
