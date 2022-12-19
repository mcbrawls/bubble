package dev.andante.bubble.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface DimensionUtil {
    static DimensionOptions createDimensionOptions(MinecraftServer server, RegistryKey<DimensionType> type, ChunkGenerator generator) {
        return new DimensionOptions(getRegistryEntry(server, type), generator);
    }

    static RegistryEntry<DimensionType> getRegistryEntry(MinecraftServer server, RegistryKey<DimensionType> type) {
        return server.getRegistryManager()
                     .get(RegistryKeys.DIMENSION_TYPE)
                     .getEntry(type)
                     .orElseThrow(() -> new IllegalArgumentException("Could not fetch registry entry"));
    }
}
