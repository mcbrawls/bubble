package dev.andante.bubble.mixin;

import dev.andante.bubble.world.TemporaryWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.ChunkDataList;
import net.minecraft.world.storage.EntityChunkDataAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityChunkDataAccess.class)
public class EntityChunkDataAccessMixin {
    @Shadow @Final private ServerWorld world;

    /**
     * Cancels saving of entities for temporary worlds.
     */
    @Inject(method = "writeChunkData", at = @At("HEAD"), cancellable = true)
    private void onWriteChunkData(ChunkDataList<Entity> dataList, CallbackInfo ci) {
        if (this.world instanceof TemporaryWorld) {
            ci.cancel();
        }
    }
}
