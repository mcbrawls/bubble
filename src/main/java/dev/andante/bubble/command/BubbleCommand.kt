package dev.andante.bubble.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import dev.andante.bubble.Bubble
import dev.andante.bubble.BubbleManager
import dev.andante.bubble.world.BubbleWorld
import net.minecraft.command.argument.DimensionArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object BubbleCommand {
    private val INVALID_DIMENSION_EXCEPTION = DynamicCommandExceptionType { id -> Text.translatable("argument.dimension.invalid", id) }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("bubble-test")
                .requires { source -> source.hasPermissionLevel(2) }
                .then(
                    CommandManager.literal("add")
                        .executes(::executeAdd)
                )
                .then(
                    CommandManager.literal("delete")
                        .then(
                            CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                .suggests(Bubble::suggestBubbleDimensions)
                                .executes(::executeDelete)
                        )
                )
        )
    }

    private fun executeAdd(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val server = source.server
        val bubbleManager = BubbleManager.getOrCreate(server)
        val world = bubbleManager.createAndInitialize()
        source.sendFeedback({ Text.literal("Successfully added dimension '${world.worldRegistryKey.value}'") }, true)
        return 1
    }

    @Throws(CommandSyntaxException::class)
    fun executeDelete(context: CommandContext<ServerCommandSource>): Int {
        val world = DimensionArgumentType.getDimensionArgument(context, "dimension")
        return if (world is BubbleWorld) {
            val source = context.source
            val server = source.server
            val bubbleManager = BubbleManager.getOrCreate(server)
            bubbleManager.remove(world)
            source.sendFeedback({ Text.literal("Successfully deleted dimension '${world.worldRegistryKey.value}'")}, true)
            1
        } else {
            throw INVALID_DIMENSION_EXCEPTION.create(world.registryKey.value)
        }
    }
}
