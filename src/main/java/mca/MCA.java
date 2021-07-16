package mca;

import mca.block.BlocksMCA;
import mca.entity.EntitiesMCA;
import mca.item.ItemsMCA;
import mca.network.MessagesMCA;
import mca.resources.ApiReloadListener;
import mca.server.ReaperSpawner;
import mca.server.ServerInteractionManager;
import mca.server.command.AdminCommand;
import mca.server.command.MCACommand;
import mca.server.world.data.BabyBunker;
import mca.server.world.data.VillageManagerData;
import mca.server.world.village.VillageHelper;
import mca.server.world.village.VillageSpawnQueue;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MCA implements ModInitializer {
    public static final String MOD_ID = "mca";
    public static final Logger logger = LogManager.getLogger();

    private static Config config = new Config();

    public static Config getConfig() {
        return config;
    }

    @Override
    public void onInitialize() {
        BlocksMCA.bootstrap();
        ItemsMCA.bootstrap();
        SoundsMCA.bootstrap();
        ParticleTypesMCA.bootstrap();
        EntitiesMCA.bootstrap();
        MessagesMCA.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ApiReloadListener());

        ServerTickEvents.END_WORLD_TICK.register(w -> {
            VillageHelper.tick(w);

            if (w.getTime() % 21 == 0) {
                VillageManagerData.get(w).processNextBuildings(w);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            ReaperSpawner.tick();
            ServerInteractionManager.getInstance().tick();
            VillageSpawnQueue.getInstance().tick();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((old, neu, alive) -> {
            if (!alive) {
                BabyBunker.get((ServerWorld)old.world).pop(neu);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            AdminCommand.register(dispatcher);
            MCACommand.register(dispatcher);
        });
    }
}
