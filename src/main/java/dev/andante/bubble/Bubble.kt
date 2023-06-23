package dev.andante.bubble

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.andante.bubble.command.BubbleCommand
import dev.andante.bubble.world.BubbleWorld
import dev.andante.bubble.world.property.VoidChunkGenerator
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import net.minecraft.world.dimension.DimensionType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * The Bubble entry point.
 */
object Bubble : ModInitializer {
    const val MOD_ID = "bubble"
    const val MOD_NAME = "Bubble"

    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    val DEFAULT_DIMENSION_TYPE: RegistryKey<DimensionType> = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier(MOD_ID, "default"))

    override fun onInitialize() {
        LOGGER.info("Initializing $MOD_NAME")

        // chunk generator
        Registry.register(Registries.CHUNK_GENERATOR, Identifier(MOD_ID, "void"), VoidChunkGenerator.CODEC)

        // initialize
        BubbleManager

        // register commands
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> BubbleCommand.register(dispatcher) }
    }

    /**
     * Suggests bubble worlds to a command context.
     */
    fun suggestBubbleDimensions(context: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        CommandSource.suggestIdentifiers(
            context.source.server.worlds
                .filterValues { it is BubbleWorld }
                .keys
                .map(RegistryKey<*>::getValue),
            builder
        )

        return builder.buildFuture()
    }
}
