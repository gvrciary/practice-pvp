package alexis.practice.party.games.event.ffa;

import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PartyFFATypes {
    NODEBUFF("nodebuff"),
    SUMO("sumo"),
    TNTSUMO("tntsumo"),
    COMBO("combo"),
    HG("hg"),
    MIDFIGHT("midfight"),
    BUILDUHC("builduhc"),
    FINALUHC("finaluhc"),
    CAVEUHC("caveuhc");

    private final Kit type;
    private final Class<? extends PartyFFA> classDuel;

    PartyFFATypes(String type){
        this.type = KitManager.getInstance().getKit(type);

        if (this.type == null) {
            throw new IllegalArgumentException("PartySplitType: Kit not found: " + type);
        }

        this.classDuel = PartyFFA.class;
    }

    public String getName() {
        return type.getName();
    }

    public String getCustomName() {
        return type.getCustomName();
    }

    public static PartyFFATypes get(String typeName) {
        return Arrays.stream(PartyFFATypes.values())
                .filter(duelType -> duelType.getType().getName().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
