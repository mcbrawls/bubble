package net.mcdivisions.bubble.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@SuppressWarnings("unchecked")
public interface RemovableSimpleRegistry<T> {
    static <T> void remove(SimpleRegistry<T> registry, Identifier id) {
        ((RemovableSimpleRegistry<T>) registry).remove(id);
    }

    static <T> void remove(SimpleRegistry<T> registry, RegistryKey<World> key) {
        remove(registry, key.getValue());
    }

    boolean remove(T value);
    boolean remove(Identifier key);

    void setFrozen(boolean value);
    boolean isFrozen();
}
