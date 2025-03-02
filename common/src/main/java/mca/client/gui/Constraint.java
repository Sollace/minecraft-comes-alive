package mca.client.gui;

import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.Rank;
import mca.entity.ai.Relationship;
import mca.entity.ai.relationship.AgeState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Constraint implements BiPredicate<VillagerLike<?>, Entity> {
    FAMILY("family", Relationship.IS_FAMILY.asConstraint()),
    NOT_FAMILY("!family", Relationship.IS_FAMILY.negate().asConstraint()),

    ADULT("adult", (villager, player) -> villager.getAgeState() == AgeState.ADULT),
    NOT_ADULT("!adult", (villager, player) -> villager.getAgeState() != AgeState.ADULT),

    SPOUSE("spouse", Relationship.IS_MARRIED.asConstraint()),
    NOT_SPOUSE("!spouse", Relationship.IS_MARRIED.negate().asConstraint()),

    KIDS("kids", Relationship.IS_PARENT.asConstraint()),
    NOT_KIDS("!kids", Relationship.IS_PARENT.negate().asConstraint()),

    CLERIC("cleric", (villager, player) -> villager.getVillagerData().getProfession() == VillagerProfession.CLERIC),
    NOT_CLERIC("!cleric", (villager, player) -> villager.getVillagerData().getProfession() != VillagerProfession.CLERIC),

    OUTLAWED("outlawed", (villager, player) -> villager.getVillagerData().getProfession() == ProfessionsMCA.OUTLAW),
    NOT_OUTLAWED("!outlawed", (villager, player) -> villager.getVillagerData().getProfession() == ProfessionsMCA.OUTLAW),

    PEASANT("peasant", (villager, player) -> {
        return player instanceof PlayerEntity && villager instanceof VillagerEntityMCA && ((VillagerEntityMCA)villager).getResidency().getHomeVillage().filter(village -> {
            return village.getRank((PlayerEntity)player).getReputation() >= Rank.PEASANT.getReputation();
        }).isPresent();
    }),
    NOT_PEASANT("!peasant", (villager, player) -> {
        return !(player instanceof PlayerEntity) || !(villager instanceof VillagerEntityMCA) || !((VillagerEntityMCA)villager).getResidency().getHomeVillage().filter(village -> {
            return village.getRank((PlayerEntity)player).getReputation() >= Rank.PEASANT.getReputation();
        }).isPresent();
    });

    public static final Map<String, Constraint> REGISTRY = Stream.of(values()).collect(Collectors.toMap(a -> a.id, Function.identity()));

    private final String id;
    private final BiPredicate<VillagerLike<?>, Entity> check;

    Constraint(String id, BiPredicate<VillagerLike<?>, Entity> check) {
        this.id = id;
        this.check = check;
    }

    @Override
    public boolean test(VillagerLike<?> t, Entity u) {
        return check.test(t, u);
    }

    public static Set<Constraint> all() {
        return new HashSet<>(REGISTRY.values());
    }

    public static Set<Constraint> allMatching(VillagerLike<?> villager, Entity player) {
        return Stream.of(values()).filter(c -> c.test(villager, player)).collect(Collectors.toSet());
    }

    public static List<Constraint> fromStringList(String constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return new ArrayList<>();
        }
        return Stream.of(constraints.split("\\,"))
                .map(REGISTRY::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

