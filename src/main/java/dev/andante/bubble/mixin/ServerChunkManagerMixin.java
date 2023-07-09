package dev.andante.bubble.mixin;

import dev.andante.bubble.world.IBubbleWorld;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Shadow @Final ServerWorld world;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;mkdirs()Z"
            )
    )
    private boolean onInitMkDirs(File file) {
        if (this.world instanceof IBubbleWorld) {
            return false;
        }

        return file.mkdirs();
    }
}
