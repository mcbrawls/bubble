package net.mcdivisions.bubble.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.mcdivisions.bubble.world.TemporaryWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public class TemporaryDimensionArgumentType extends DimensionArgumentType {
    public static DimensionArgumentType temporaryDimension() {
        return new TemporaryDimensionArgumentType();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource source ? CommandSource.suggestIdentifiers(
            source.getWorldKeys()
                  .stream()
                  .filter(key -> {
                      World world = source.getRegistryManager().get(RegistryKeys.WORLD).get(key);
                      return world instanceof TemporaryWorld;
                  })
                  .map(RegistryKey::getValue),
            builder
        ) : Suggestions.empty();
    }
}
