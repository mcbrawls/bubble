package net.mcdivisions.bubble.util;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredRegistry<T> extends SimpleRegistry<T> {
    private final Registry<T> parent;
    private final Predicate<T> filter;

    public FilteredRegistry(Registry<T> parent, Predicate<T> filter) {
        super(parent.getKey(), parent.getLifecycle());
        this.parent = parent;
        this.filter = filter;
    }

    public Registry<T> getParent() {
        return this.parent;
    }

    @Nullable
    @Override
    public Identifier getId(T value) {
        return this.filter.test(value) ? this.parent.getId(value) : null;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return this.filter.test(entry) ? this.parent.getKey(entry) : Optional.empty();
    }

    @Override
    public int getRawId(@Nullable T value) {
        return this.filter.test(value) ? this.parent.getRawId(value) : -1;
    }

    @Nullable
    @Override
    public T get(int index) {
        return this.parent.get(index);
    }

    @Override
    public int size() {
        return this.parent.size();
    }

    @Nullable
    @Override
    public T get(@Nullable RegistryKey<T> key) {
        return this.parent.get(key);
    }

    @Override
    public T get(@Nullable Identifier id) {
        return null;
    }

    @Override
    public Lifecycle getEntryLifecycle(T entry) {
        return this.parent.getEntryLifecycle(entry);
    }

    @Override
    public Lifecycle getLifecycle() {
        return this.parent.getLifecycle();
    }

    @Override
    public Set<Identifier> getIds() {
        return Collections.emptySet();
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        Set<Map.Entry<RegistryKey<T>, T>> set = new HashSet<>();
        for (Map.Entry<RegistryKey<T>, T> e : this.parent.getEntrySet()) {
            if (this.filter.test(e.getValue())) {
                set.add(e);
            }
        }
        return set;
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return null;
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        return Optional.empty();
    }

    @Override
    public boolean containsId(Identifier id) {
        return this.parent.containsId(id);
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
        return this.parent.contains(key);
    }

    @Override
    public Registry<T> freeze() {
        return this;
    }

    @Override
    public RegistryEntry.Reference<T> createEntry(T value) {
        return null;
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        return this.parent.getEntry(rawId);
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key) {
        return this.parent.getEntry(key);
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return this.parent.streamEntries().filter((e) -> this.filter.test(e.value()));
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
        return Optional.empty();
    }

    @Override
    public RegistryEntryList.Named<T> getOrCreateEntryList(TagKey<T> tag) {
        return null;
    }

    @Override
    public Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
        return Stream.empty();
    }

    @Override
    public Stream<TagKey<T>> streamTags() {
        return Stream.empty();
    }

    @Override
    public void clearTags() {
    }

    @Override
    public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> tagEntries) {
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.parent.iterator(), this.filter::test);
    }
}
