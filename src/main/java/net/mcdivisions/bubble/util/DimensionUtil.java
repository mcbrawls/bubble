package net.mcdivisions.bubble.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface DimensionUtil {
    static DimensionOptions createDimensionOptions(MinecraftServer server, RegistryKey<DimensionType> type, ChunkGenerator generator) {
        return new DimensionOptions(getRegistryEntry(server, type), generator);
    }

    static RegistryEntry<DimensionType> getRegistryEntry(MinecraftServer server, RegistryKey<DimensionType> type) {
        return server.getRegistryManager()
                     .get(Registry.DIMENSION_TYPE_KEY)
                     .getEntry(type)
                     .orElseThrow(() -> new IllegalArgumentException("Could not fetch registry entry"));
    }
}
