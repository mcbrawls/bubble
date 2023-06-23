package dev.andante.bubble.world.property

import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import net.minecraft.world.SaveProperties
import net.minecraft.world.level.UnmodifiableLevelProperties

class DefaultedLevelProperties(saveProperties: SaveProperties) : UnmodifiableLevelProperties(saveProperties, saveProperties.mainWorldProperties) {
    private val gameRules = GameRules()
    override fun getGameRules(): GameRules {
        return gameRules
    }

    override fun setTimeOfDay(timeOfDay: Long) {}
    override fun getTimeOfDay(): Long {
        return 6000
    }

    override fun setClearWeatherTime(time: Int) {}
    override fun getClearWeatherTime(): Int {
        return 0
    }

    override fun setRaining(raining: Boolean) {}
    override fun isRaining(): Boolean {
        return false
    }

    override fun setRainTime(time: Int) {}
    override fun getRainTime(): Int {
        return 0
    }

    override fun setThundering(thundering: Boolean) {}
    override fun isThundering(): Boolean {
        return false
    }

    override fun setThunderTime(time: Int) {}
    override fun getThunderTime(): Int {
        return 0
    }

    override fun getDifficulty(): Difficulty {
        return Difficulty.NORMAL
    }
}
