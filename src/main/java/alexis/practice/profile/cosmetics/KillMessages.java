package alexis.practice.profile.cosmetics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum KillMessages {
    HUMILIATED("Humiliated", "&7was utterly humiliated by"),
    DESTROYED("Destroyed", "&7was completely destroyed by"),
    CRUSHED("Crushed", "&7was crushed without mercy by"),
    ANNIHILATED("Annihilated", "&7met their end in total annihilation at the hands of"),
    OBLITERATED("Obliterated", "&7was obliterated beyond recognition by"),
    DOMINATED("Dominated", "&7was dominated and left in ruins by"),
    OUTPLAYED("Outplayed", "&7was outplayed and sent to the afterlife by");

    private final String name;
    private final String format;

    public static KillMessages get(String typeName) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(typeName))
                .findFirst()
                .orElse(null);
    }
}
