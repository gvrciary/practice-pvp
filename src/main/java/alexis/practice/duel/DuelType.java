package alexis.practice.duel;

import alexis.practice.duel.types.BattleRush;
import alexis.practice.duel.types.BedFight;
import alexis.practice.duel.types.Boxing;
import alexis.practice.duel.types.Bridge;
import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DuelType {
    NODEBUFF("nodebuff"),
    FINALUHC("finaluhc"),
    MIDFIGHT("midfight"),
    SUMO("sumo"),
    BUILDUHC("builduhc"),
    CAVEUHC("caveuhc"),
    COMBO("combo"),
    BEDFIGHT("bedfight", BedFight.class),
    FIREBALL("fireball", BedFight.class),
    BRIDGE("bridge", Bridge.class),
    HG("hg"),
    TNTSUMO("tntsumo"),
    BATTLERUSH("battlerush", BattleRush.class),
    BOXING("boxing", Boxing.class);

    private final Kit type;
    private final Class<? extends Duel> classDuel;

    DuelType(String type, Class<? extends Duel> classDuel) {
        this.type = KitManager.getInstance().getKit(type);

        if (this.type == null) {
            throw new IllegalArgumentException("DuelType: Kit not found: " + type);
        }

        this.classDuel = classDuel;
    }

    DuelType(String type){
        this(type, Duel.class);
    }

    public String getName() {
        return type.getName();
    }

    public String getCustomName() {
        return type.getCustomName();
    }

    public static DuelType get(String typeName) {
        return Arrays.stream(values())
                .filter(duelType -> duelType.getType().getName().equals(typeName))
                .findFirst()
                .orElse(null);
    }
}
