package net.mcdivisions.bubble.world;

public interface BubbleDimensionOptionsAccess {
    void setShouldSave(boolean shouldSave);
    boolean shouldSave();

    static <T> boolean shouldSave(T value) {
        return value instanceof BubbleDimensionOptionsAccess access && access.shouldSave();
    }
}
