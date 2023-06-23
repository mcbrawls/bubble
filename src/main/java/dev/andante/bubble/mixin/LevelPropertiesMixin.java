package dev.andante.bubble.mixin;

import dev.andante.bubble.Bubble;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelProperties.class)
public class LevelPropertiesMixin {
    @Shadow @Final protected static String WORLD_GEN_SETTINGS_KEY;

    /**
     * Removes bubble worlds from being saved as dimensions.
     */
    @Inject(
            method = "updateProperties",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelInfo;getGameMode()Lnet/minecraft/world/GameMode;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void onUpdateProperties(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci) {
        try {
            NbtCompound worldGenSettingsNbt = levelNbt.getCompound(WORLD_GEN_SETTINGS_KEY);
            NbtCompound dimensionsNbt = worldGenSettingsNbt.getCompound("dimensions");
            List<String> forRemoval = dimensionsNbt.getKeys().stream().filter(key -> {
                Identifier id = Identifier.tryParse(key);
                return id != null && id.getNamespace().equals(Bubble.MOD_ID);
            }).toList();
            forRemoval.forEach(dimensionsNbt::remove);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
