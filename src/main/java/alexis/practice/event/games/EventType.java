package alexis.practice.event.games;

import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.skywars.Skywars;
import alexis.practice.event.games.types.sumo.Sumo;
import alexis.practice.event.games.types.tournament.Tournament;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum EventType {
    SKYWARS("SkyWars", Skywars.class, "textures/items/gold_axe.png"),
    SUMO("Sumo", Sumo.class, "textures/items/slimeball.png"),
    TOURNAMENT("Tournament", Tournament.class, "textures/items/totem.png"),
    MEETUP("Meetup", Meetup.class, "textures/items/diamond_sword.png");

    private final String name;
    private final Class<? extends EventArena> eventClass;
    private final String image;

    public static EventType get(String typeName) {
        return Arrays.stream(values())
                .filter(type -> type.getName().equals(typeName))
                .findFirst()
                .orElse(null);
    }
}
