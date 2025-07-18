package alexis.practice.party.games.duel;

import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import alexis.practice.party.games.duel.types.BattleRush;
import alexis.practice.party.games.duel.types.BedFight;
import alexis.practice.party.games.duel.types.Boxing;
import alexis.practice.party.games.duel.types.Bridge;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PartyDuelTypes {
    NODEBUFF("nodebuff"),
    BUILDUHC("builduhc"),
    COMBO("combo"),
    HG("hg"),
    SUMO("sumo"),
    MIDFIGHT("midfight"),
    FINALUHC("finaluhc"),
    CAVEUHC("caveuhc"),
    TNTSUMO("tntsumo"),
    BATTLERUSH("battlerush", BattleRush.class),
    BEDFIGHT("bedfight", BedFight.class),
    FIREBALL("fireball", BedFight.class),
    BOXING("boxing", Boxing.class),
    BRIDGE("bridge", Bridge.class);

    private final Kit type;
    private final Class<? extends PartyDuel> classDuel;

    PartyDuelTypes(String type, Class<? extends PartyDuel> classDuel) {
        this.type = KitManager.getInstance().getKit(type);

        if (this.type == null) {
            throw new IllegalArgumentException("PartyDuelType: Kit not found: " + type);
        }

        this.classDuel = classDuel;
    }

    PartyDuelTypes(String type){
        this(type, PartyDuel.class);
    }

    public String getName() {
        return type.getName();
    }

    public String getCustomName() {
        return type.getCustomName();
    }

    public static PartyDuelTypes get(String typeName) {
        return Arrays.stream(PartyDuelTypes.values())
                .filter(duelType -> duelType.getType().getName().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
