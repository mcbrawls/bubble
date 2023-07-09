package dev.andante.bubble.mixin;

import dev.andante.bubble.world.IBubbleWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final public Map<RegistryKey<World>, ServerWorld> worlds;

    @Unique
    private final Map<RegistryKey<World>, IBubbleWorld> temporaryBubbleWorldsStorage = new HashMap<>();

    /**
     * Removes bubble worlds before saving.
     */
    @SuppressWarnings("resource")
    @Inject(method = "save", at = @At("HEAD"))
    private void onSaveHead(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        temporaryBubbleWorldsStorage.clear();

        new HashMap<>(worlds).forEach((key, world) -> {
            if (world instanceof IBubbleWorld bubbleWorld) {
                temporaryBubbleWorldsStorage.put(key, bubbleWorld);
                worlds.remove(key);
            }
        });
    }

    /**
     * Add bubble worlds back after saving.
     */
    @Inject(method = "save", at = @At("TAIL"))
    private void onSaveTail(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        temporaryBubbleWorldsStorage.forEach((key, world) -> worlds.put(key, world.asServerWorld()));
        temporaryBubbleWorldsStorage.clear();
    }
}
