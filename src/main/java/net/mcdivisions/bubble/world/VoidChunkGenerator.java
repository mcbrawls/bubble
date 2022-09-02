package net.mcdivisions.bubble.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VoidChunkGenerator extends ChunkGenerator {
    public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Biome.REGISTRY_CODEC.stable()
                                    .fieldOf("biome")
                                    .forGetter(generator -> generator.biome)
        ).apply(instance, instance.stable(VoidChunkGenerator::new));
    });

    private static final VerticalBlockSample EMPTY_SAMPLE = new VerticalBlockSample(0, new BlockState[0]);
    private static final Registry<StructureSet> EMPTY_STRUCTURE_REGISTRY = new SimpleRegistry<>(Registry.STRUCTURE_SET_KEY, Lifecycle.stable(), value -> null).freeze();

    private final RegistryEntry<Biome> biome;

    public VoidChunkGenerator(RegistryEntry<Biome> biome) {
        super(EMPTY_STRUCTURE_REGISTRY, Optional.empty(), new FixedBiomeSource(biome));
        this.biome = biome;
    }

    public VoidChunkGenerator(Registry<Biome> registry, RegistryKey<Biome> biome) {
        this(registry.getEntry(biome).orElseThrow(() -> new IllegalArgumentException("Could not fetch biome " + biome.getValue())));
    }

    public VoidChunkGenerator(Registry<Biome> registry) {
        this(registry, BiomeKeys.THE_VOID);
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void carve(ChunkRegion region, long seed, NoiseConfig config, BiomeAccess world, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver step) {
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig config, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig config) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig config) {
        return EMPTY_SAMPLE;
    }

    @Override
    public void getDebugHudText(List<String> list, NoiseConfig config, BlockPos pos) {
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig config, Chunk chunk) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getWorldHeight() {
        return 0;
    }

    @Nullable
    @Override
    public Pair<BlockPos, RegistryEntry<Structure>> locateStructure(ServerWorld world, RegistryEntryList<Structure> structures, BlockPos center, int radius, boolean skipReferencedStructures) {
        return null;
    }

    @Override
    public boolean shouldStructureGenerateInRange(RegistryEntry<StructureSet> structureSet, NoiseConfig config, long seed, int chunkX, int chunkZ, int chunkRange) {
        return false;
    }

    @Override
    public Pool<SpawnSettings.SpawnEntry> getEntitySpawnList(RegistryEntry<Biome> biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
        return Pool.empty();
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager dynamicRegistryManager, NoiseConfig config, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager, long seed) {
    }

    @Nullable
    @Override
    public List<ChunkPos> getConcentricRingsStartChunks(ConcentricRingsStructurePlacement structurePlacement, NoiseConfig config) {
        return Collections.emptyList();
    }
}
