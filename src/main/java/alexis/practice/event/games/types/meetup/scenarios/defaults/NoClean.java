package alexis.practice.event.games.types.meetup.scenarios.defaults;

import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import alexis.practice.profile.Profile;
import cn.nukkit.item.Item;

import java.util.HashMap;
import java.util.Map;

public class NoClean extends Scenario {
    private static final int TIME = 20;

    private final Map<String, Long> cache = new HashMap<>();

    public NoClean(Meetup eventArena, String name, String description, Item item, boolean enabled) {
        super(eventArena, name, description, item, enabled);
    }

    public boolean isInNoClean(Profile profile) {
        return cache.containsKey(profile.getIdentifier());
    }

    public boolean isValid(Profile profile) {
        return isInNoClean(profile) && get(profile) > System.currentTimeMillis();
    }

    public Long get(Profile profile) {
        return cache.get(profile.getIdentifier());
    }

    public void remove(Profile profile) {
        cache.remove(profile.getIdentifier());
    }

    public void setNoClean(Profile profile) {
        cache.put(profile.getIdentifier(), System.currentTimeMillis() + TIME * 1000L);
    }
}
