package mca.entity.ai;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

import com.mojang.serialization.Codec;

import mca.MCA;

public interface MemoryModuleTypeMCA {
    //if you do not provide a codec, it does not save! however, for things like players, you will likely need to save their UUID beforehand.
    MemoryModuleType<PlayerEntity> PLAYER_FOLLOWING = register("player_following_memory", Optional.empty());
    MemoryModuleType<Boolean> STAYING = register("staying_memory", Optional.of(Codec.BOOL));

    static void bootstrap() { }

    private static <U> MemoryModuleType<U> register(String name, Optional<Codec<U>> codec) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new Identifier(MCA.MOD_ID, name), new MemoryModuleType<>(codec));
    }
}