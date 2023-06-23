package dev.andante.bubble.world.property

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureSet
import net.minecraft.structure.StructureTemplateManager
import net.minecraft.util.collection.Pool
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ChunkRegion
import net.minecraft.world.HeightLimitView
import net.minecraft.world.Heightmap
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.biome.SpawnSettings.SpawnEntry
import net.minecraft.world.biome.source.BiomeAccess
import net.minecraft.world.biome.source.FixedBiomeSource
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.chunk.Blender
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.chunk.VerticalBlockSample
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator
import net.minecraft.world.gen.noise.NoiseConfig
import net.minecraft.world.gen.structure.Structure
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Function
import java.util.stream.Stream

class VoidChunkGenerator(private val biome: RegistryEntry<Biome>) : ChunkGenerator(FixedBiomeSource(biome)) {
    constructor(registry: Registry<Biome>, biome: RegistryKey<Biome> = BiomeKeys.THE_VOID) : this(
        registry.getEntry(biome).orElseThrow { IllegalArgumentException("Could not fetch biome ${biome.value}") }
    )

    override fun getCodec(): Codec<out ChunkGenerator> {
        return CODEC
    }

    override fun carve(
        region: ChunkRegion,
        seed: Long,
        config: NoiseConfig,
        world: BiomeAccess,
        structureAccessor: StructureAccessor,
        chunk: Chunk,
        step: GenerationStep.Carver
    ) {
    }

    override fun addStructureReferences(world: StructureWorldAccess, accessor: StructureAccessor, chunk: Chunk) {}
    override fun populateNoise(
        executor: Executor,
        blender: Blender,
        config: NoiseConfig,
        structureAccessor: StructureAccessor,
        chunk: Chunk
    ): CompletableFuture<Chunk> {
        return CompletableFuture.completedFuture(chunk)
    }

    override fun getSeaLevel(): Int {
        return 0
    }

    override fun getMinimumY(): Int {
        return 0
    }

    override fun getHeight(
        x: Int,
        z: Int,
        heightmap: Heightmap.Type,
        world: HeightLimitView,
        config: NoiseConfig
    ): Int {
        return 0
    }

    override fun getColumnSample(x: Int, z: Int, world: HeightLimitView, config: NoiseConfig): VerticalBlockSample {
        return EMPTY_SAMPLE
    }

    override fun getDebugHudText(list: List<String>, config: NoiseConfig, pos: BlockPos) {}
    override fun generateFeatures(world: StructureWorldAccess, chunk: Chunk, structureAccessor: StructureAccessor) {}
    override fun buildSurface(region: ChunkRegion, structures: StructureAccessor, config: NoiseConfig, chunk: Chunk) {}
    override fun populateEntities(region: ChunkRegion) {}
    override fun getWorldHeight(): Int {
        return 0
    }

    override fun locateStructure(
        world: ServerWorld,
        structures: RegistryEntryList<Structure>,
        center: BlockPos,
        radius: Int,
        skipReferencedStructures: Boolean
    ): Pair<BlockPos, RegistryEntry<Structure>>? {
        return null
    }

    override fun getEntitySpawnList(
        biome: RegistryEntry<Biome>,
        accessor: StructureAccessor,
        group: SpawnGroup,
        pos: BlockPos
    ): Pool<SpawnEntry> {
        return Pool.empty()
    }

    override fun setStructureStarts(
        registryManager: DynamicRegistryManager,
        placementCalculator: StructurePlacementCalculator,
        structureAccessor: StructureAccessor,
        chunk: Chunk,
        structureTemplateManager: StructureTemplateManager
    ) {
    }

    override fun createStructurePlacementCalculator(
        structureSetRegistry: RegistryWrapper<StructureSet>,
        noiseConfig: NoiseConfig,
        seed: Long
    ): StructurePlacementCalculator {
        return StructurePlacementCalculator.create(noiseConfig, seed, biomeSource, Stream.empty())
    }

    companion object {
        val CODEC: Codec<VoidChunkGenerator> = RecordCodecBuilder.create { instance ->
            instance.group(
                Biome.REGISTRY_CODEC.stable()
                    .fieldOf("biome")
                    .forGetter { generator: VoidChunkGenerator -> generator.biome }
            ).apply(
                instance, instance.stable(
                    Function { biome: RegistryEntry<Biome> -> VoidChunkGenerator(biome) })
            )
        }

        private val EMPTY_SAMPLE = VerticalBlockSample(0, arrayOfNulls(0))
    }
}
