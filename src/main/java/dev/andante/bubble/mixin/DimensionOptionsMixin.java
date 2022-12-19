package dev.andante.bubble.mixin;

import dev.andante.bubble.world.BubbleDimensionOptionsAccess;
import net.minecraft.world.dimension.DimensionOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DimensionOptions.class)
public class DimensionOptionsMixin implements BubbleDimensionOptionsAccess {
    @Unique private boolean shouldSave = true;

    @Unique
    @Override
    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    @Unique
    @Override
    public boolean shouldSave() {
        return this.shouldSave;
    }
}
