package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.FamilyTreeEntry;
import mca.enums.Constraint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GetInteractDataRequest implements Message {
    private static final long serialVersionUID = -4363277735373237564L;

    UUID uuid;

    public GetInteractDataRequest(UUID villager) {
        this.uuid = villager;
    }

    @Override
    public void receive(PlayerEntity player) {
        Entity entity = ((ServerWorld) player.world).getEntity(uuid);

        if (entity instanceof VillagerEntityMCA) {
            VillagerEntityMCA villager = (VillagerEntityMCA) entity;

            //get constraints
            Map<String, Boolean> constraints = new HashMap<>();
            for (Constraint c : Constraint.values()) {
                constraints.put(c.getId(), c.getCheck().test(villager, player));
            }

            //also pack
            FamilyTreeEntry familyTreeEntry = villager.getFamilyTreeEntry();
            FamilyTreeEntry father = familyTreeEntry != null ? villager.getFamilyTree().getEntry(familyTreeEntry.getFather()) : null;
            String fatherName = father != null ? father.getName() : null;
            FamilyTreeEntry mother = familyTreeEntry != null ? villager.getFamilyTree().getEntry(familyTreeEntry.getMother()) : null;
            String motherName = mother != null ? mother.getName() : null;

            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new GetInteractDataResponse(constraints, fatherName, motherName), (ServerPlayerEntity)player);
            }
        }
    }
}