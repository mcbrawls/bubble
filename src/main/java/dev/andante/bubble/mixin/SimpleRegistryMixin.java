package dev.andante.bubble.mixin;

import dev.andante.bubble.registry.RemovableSimpleRegistry;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

/**
 * Adds support for modifying frozen registries.
 */
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RemovableSimpleRegistry<T> {
    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;
    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;

    @Shadow private boolean frozen;

    @Shadow @Final private Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;

    @Unique
    @Override
    public boolean remove(T value) {
        RegistryEntry.Reference<T> entry = this.valueToEntry.get(value);
        int rawId = this.entryToRawId.removeInt(value);
        if (rawId == -1) {
            return false;
        }

        try {
            this.rawIdToEntry.set(rawId, null);
            RegistryKey<T> key = entry.registryKey();
            this.idToEntry.remove(key.getValue());
            this.keyToEntry.remove(key);

            this.keyToEntryInfo.remove(key);
            this.valueToEntry.remove(value);

            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Unique
    @Override
    public boolean remove(@NotNull Identifier id) {
        RegistryEntry.Reference<T> entry = this.idToEntry.get(id);
        return entry != null && entry.hasKeyAndValue() && this.remove(entry.value());
    }

    @Unique
    @Override
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    @Unique
    @Override
    public boolean isFrozen() {
        return this.frozen;
    }
}
