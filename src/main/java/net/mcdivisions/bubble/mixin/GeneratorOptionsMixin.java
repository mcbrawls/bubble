package net.mcdivisions.bubble.mixin;

import net.mcdivisions.bubble.util.FilteredRegistry;
import net.mcdivisions.bubble.world.BubbleDimensionOptionsAccess;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

@Mixin(GeneratorOptions.class)
public class GeneratorOptionsMixin {
    @ModifyArg(
        method = "method_28606",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/MapCodec;forGetter(Ljava/util/function/Function;)Lcom/mojang/serialization/codecs/RecordCodecBuilder;",
            ordinal = 3
        )
    )
    private static Function<GeneratorOptions, Registry<DimensionOptions>> fantasy$wrapRegistry(Function<GeneratorOptions, Registry<DimensionOptions>> getter) {
        return option -> new FilteredRegistry<>(option.getDimensions(), BubbleDimensionOptionsAccess::shouldSave);
    }
}
