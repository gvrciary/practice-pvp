package alexis.practice.event.games.types.meetup.scenarios;

import alexis.practice.event.games.types.meetup.Meetup;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Scenario {
    protected final Meetup eventArena;
    @Getter
    private final String name;
    @Getter
    private final String description;
    private final Item item;
    @Getter
    private boolean enabled;

    public void add(Player player) {}

    public void toggle() {
        enabled = !enabled;

        eventArena.getEvent().broadcast("&7" + name + " has been " + (enabled ? "&aEnabled" : "&cDisabled"));
    }

    public Item getItem() {
        return item.setCustomName(TextFormat.colorize("&r&6" + name));
    }

    public void destroy() {}

}
