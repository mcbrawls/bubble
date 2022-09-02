package net.mcdivisions.bubble.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.mcdivisions.bubble.BubbleManager;
import net.mcdivisions.bubble.world.TemporaryWorld;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class BubbleCommand {
    public static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("argument.dimension.invalid", id));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("bubble")
                .then(
                    literal("delete")
                        .then(
                            argument("dimension", TemporaryDimensionArgumentType.dimension())
                                .executes(BubbleCommand::executeDelete)
                        )
                )
                .then(
                    literal("add")
                        .executes(BubbleCommand::executeAdd)
                )
        );
    }

    private static int executeAdd(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();
        BubbleManager bubbleManager = BubbleManager.getOrCreate(server);
        TemporaryWorld world = bubbleManager.createAndInitialize();
        source.sendFeedback(Text.literal("Successfully added dimension '%s'".formatted(world.getRegistryKey().getValue())), true);
        return 1;
    }

    public static int executeDelete(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "dimension");
        if (world instanceof TemporaryWorld temporaryWorld) {
            ServerCommandSource source = context.getSource();
            MinecraftServer server = source.getServer();
            BubbleManager bubbleManager = BubbleManager.getOrCreate(server);
            bubbleManager.scheduleDelete(temporaryWorld);
            source.sendFeedback(Text.literal("Successfully deleted dimension '%s'".formatted(temporaryWorld.getRegistryKey().getValue())), true);
            return 1;
        } else {
            throw INVALID_DIMENSION_EXCEPTION.create(world.getRegistryKey().getValue());
        }
    }
}
