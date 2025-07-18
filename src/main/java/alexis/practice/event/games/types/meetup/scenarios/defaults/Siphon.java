package alexis.practice.event.games.types.meetup.scenarios.defaults;

import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;

public class Siphon extends Scenario {
    private static final int DURATION = 300;

    public Siphon(Meetup eventArena, String name, String description, Item item, boolean enabled) {
        super(eventArena, name, description, item, enabled);
    }

    public void add(Player player) {
        player.addEffect(Effect.getEffect(Effect.ABSORPTION).setAmplifier(1).setDuration(DURATION));
        player.addEffect(Effect.getEffect(Effect.SPEED).setAmplifier(0).setDuration(DURATION));
        player.addEffect(Effect.getEffect(Effect.STRENGTH).setAmplifier(0).setDuration(DURATION));
    }
}
