package mca.entity.interaction;

import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerLike;
import mca.network.client.OpenGuiRequest;
import mca.resources.API;
import mca.resources.ClothingList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class EntityCommandHandler<T extends Entity & VillagerLike<?>> {
    @Nullable
    protected PlayerEntity interactingPlayer;

    protected final T entity;

    public EntityCommandHandler(T entity) {
        this.entity = entity;
    }

    public Optional<PlayerEntity> getInteractingPlayer() {
        return Optional.ofNullable(interactingPlayer).filter(player -> player.currentScreenHandler != null);
    }

    public void stopInteracting() {
        if (!entity.world.isClient) {
            if (interactingPlayer instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) interactingPlayer).closeHandledScreen();
            }
        }
        interactingPlayer = null;
    }

    public ActionResult interactAt(PlayerEntity player, Vec3d pos, @NotNull Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.INTERACT, entity), (ServerPlayerEntity)player);
        }
        interactingPlayer = player;
        return ActionResult.SUCCESS;
    }

    /**
     * Called on the server to respond to button events.
     */
    public boolean handle(ServerPlayerEntity player, String command) {
        switch (command) {
            case "clothing.randClothing":
                entity.setClothes(ClothingList.getInstance().getPool(entity).pickOne());
                return true;
            case "clothing.prevClothing":
                entity.setClothes(ClothingList.getInstance().getPool(entity).pickNext(entity.getClothes(), -1));
                return true;
            case "clothing.nextClothing":
                entity.setClothes(ClothingList.getInstance().getPool(entity).pickNext(entity.getClothes(), 1));
                return true;
            case "clothing.randHair":
                entity.setHair(API.getHairPool().pickOne(entity));
                return true;
            case "clothing.prevHair":
                entity.setHair(API.getHairPool().pickNext(entity, entity.getHair(), -1));
                return true;
            case "clothing.nextHair":
                entity.setHair(API.getHairPool().pickNext(entity, entity.getHair(), 1));
                return true;
        }
        return false;
    }
}
