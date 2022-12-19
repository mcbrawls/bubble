package net.mcdivisions.bubble;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcdivisions.bubble.command.BubbleCommand;
import net.mcdivisions.bubble.world.VoidChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bubble implements ModInitializer {
    public static final String MOD_ID = "bubble";
    public static final String MOD_NAME = "Bubble";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final RegistryKey<DimensionType> DEFAULT_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier(MOD_ID, "default"));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}", MOD_NAME);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BubbleCommand.register(dispatcher);
        });

        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(MOD_ID, "void"), VoidChunkGenerator.CODEC);

        ServerTickEvents.START_SERVER_TICK.register(BubbleManager::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(BubbleManager::onServerStopping);
    }
}
