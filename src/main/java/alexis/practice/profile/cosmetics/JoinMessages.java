package alexis.practice.profile.cosmetics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum JoinMessages {
    VANILLA_MESSAGE("Vanilla Message", "%s &ahas joined the practice"),
    SPAWNED_MESSAGE("Spawned Message", "%s &ahas spawned in the server"),
    VIP_JOIN("VIP Join", "&aWelcome %s, our honored VIP!"),
    LEGEND_ARRIVAL("Legend Arrival", "%s &athe legend, has arrived!"),
    CHAMPION_ENTER("Champion Enter", "&eThe champion&f %s &ehas entered the battlefield!"),
    STAR_JOIN("Star Join", "&eShining bright,&f %s &ehas joined the game!"),
    MASTER_COMEBACK("Master Comeback", "&cBehold,&f %s &cthe master has returned!"),
    CHEATER_ALERT("Cheater Alert", "&9Watch out!&f %s &9the cheater has joined."),
    HERO_RETURN("Hero Return", "&6Our hero&f %s &6has returned!"),
    ELITE_JOIN("Elite Join", "&6The elite player&f %s &6has joined the game!");

    private final String name;
    private final String format;

    public String getMessageFormat(String name) {
        return String.format(format, name);
    }

    public static JoinMessages get(String typeName) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
