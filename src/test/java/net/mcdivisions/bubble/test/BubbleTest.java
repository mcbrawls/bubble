package net.mcdivisions.bubble.test;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mcdivisions.bubble.BubbleManager;
import net.mcdivisions.bubble.world.TemporaryWorld;
import org.slf4j.Logger;

public class BubbleTest implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            BubbleManager manager = BubbleManager.getOrCreate(server);
            TemporaryWorld deletedWorld = manager.createAndInitialize();
            LOGGER.info("{}", deletedWorld.getRegistryKey().getValue());

            TemporaryWorld world = manager.createAndInitialize();
            LOGGER.info("{}", world.getRegistryKey().getValue());

            manager.scheduleDelete(deletedWorld);
        });
    }
}
