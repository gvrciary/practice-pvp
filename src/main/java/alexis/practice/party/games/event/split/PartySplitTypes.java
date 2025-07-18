package alexis.practice.party.games.event.split;

import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import alexis.practice.party.games.event.split.types.BattleRush;
import alexis.practice.party.games.event.split.types.BedFight;
import alexis.practice.party.games.event.split.types.Boxing;
import alexis.practice.party.games.event.split.types.Bridge;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PartySplitTypes {
    NODEBUFF("nodebuff"),
    SUMO("sumo"),
    HG("hg"),
    COMBO("combo"),
    BUILDUHC("builduhc"),
    FINALUHC("finaluhc"),
    MIDFIGHT("midfight"),
    TNTSUMO("tntsumo"),
    CAVEUHC("caveuhc"),
    BATTLERUSH("battlerush", BattleRush.class),
    BEDFIGHT("bedfight", BedFight.class),
    FIREBALL("fireball", BedFight.class),
    BRIDGE("bridge", Bridge.class),
    BOXING("boxing", Boxing.class);

    private final Kit type;
    private final Class<? extends PartySplit> classDuel;

    PartySplitTypes(String type, Class<? extends PartySplit> classDuel) {
        this.type = KitManager.getInstance().getKit(type);

        if (this.type == null) {
            throw new IllegalArgumentException("PartySplitType: Kit not found: " + type);
        }

        this.classDuel = classDuel;
    }

    PartySplitTypes(String type) {
        this(type, PartySplit.class);
    }

    public String getName() {
        return type.getName();
    }

    public String getCustomName() {
        return type.getCustomName();
    }

    public static PartySplitTypes get(String typeName) {
        return Arrays.stream(PartySplitTypes.values())
                .filter(duelType -> duelType.getType().getName().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
