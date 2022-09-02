package net.mcdivisions.bubble.world;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;

public class DefaultedLevelProperties extends UnmodifiableLevelProperties {
    private final GameRules gameRules = new GameRules();

    public DefaultedLevelProperties(SaveProperties saveProperties) {
        super(saveProperties, saveProperties.getMainWorldProperties());
    }

    @Override
    public GameRules getGameRules() {
        return this.gameRules;
    }

    @Override
    public void setTimeOfDay(long timeOfDay) {
    }

    @Override
    public long getTimeOfDay() {
        return 6000;
    }

    @Override
    public void setClearWeatherTime(int time) {
    }

    @Override
    public int getClearWeatherTime() {
        return 0;
    }

    @Override
    public void setRaining(boolean raining) {
    }

    @Override
    public boolean isRaining() {
        return false;
    }

    @Override
    public void setRainTime(int time) {
    }

    @Override
    public int getRainTime() {
        return 0;
    }

    @Override
    public void setThundering(boolean thundering) {
    }

    @Override
    public boolean isThundering() {
        return false;
    }

    @Override
    public void setThunderTime(int time) {
    }

    @Override
    public int getThunderTime() {
        return 0;
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.NORMAL;
    }
}
