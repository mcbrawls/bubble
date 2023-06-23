package dev.andante.bubble.world.property

import net.minecraft.server.WorldGenerationProgressListener
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.ChunkStatus

/**
 * A world generation progress listener with empty methods.
 */
object EmptyWorldGenerationProgressListener : WorldGenerationProgressListener {
    override fun start(spawnPos: ChunkPos) {}
    override fun setChunkStatus(pos: ChunkPos, status: ChunkStatus?) {}
    override fun start() {}
    override fun stop() {}
}
