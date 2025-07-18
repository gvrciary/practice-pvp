package alexis.practice.event.games.types.meetup.scenarios.defaults;

import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import alexis.practice.event.team.Team;
import alexis.practice.profile.Profile;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;

import java.util.HashMap;
import java.util.Map;

public class SafeLoot extends Scenario {

    private final Map<String, LootCache> cache = new HashMap<>();

    public SafeLoot(Meetup eventArena, String name, String description, Item item, boolean enabled) {
        super(eventArena, name, description, item, enabled);
    }

    public boolean isOwner(Profile profile, Location location) {
        Location finalLocation = normalizeLocation(location);

        if (eventArena.getEvent().isTeam()) {
            Team teamProfile = eventArena.getEvent().getTeamManager().getTeam(profile);

            LootCache lootCache = getLootCache(String.valueOf(teamProfile.getId()));
            return lootCache.isValidLocation(finalLocation);
        }

        LootCache lootCache = getLootCache(profile.getIdentifier());
        return lootCache.isValidLocation(finalLocation);
    }

    public LootCache getLootCache(String id) {
        return cache.computeIfAbsent(id, k -> new LootCache());
    }

    public void remove(Profile profile, Location location) {
        Location finalLocation = normalizeLocation(location);

        if (eventArena.getEvent().isTeam()) {
            Team teamProfile = eventArena.getEvent().getTeamManager().getTeam(profile);

            if (teamProfile == null) return;

            LootCache lootCache = getLootCache(String.valueOf(teamProfile.getId()));
            lootCache.removeLocation(finalLocation);
            return;
        }

        LootCache lootCache = getLootCache(profile.getIdentifier());
        lootCache.removeLocation(finalLocation);
    }

    public void lock(Profile profile, Location location) {
        Location finalLocation = normalizeLocation(location);

        if (eventArena.getEvent().isTeam()) {
            Team teamProfile = eventArena.getEvent().getTeamManager().getTeam(profile);

            if (teamProfile == null) return;

            LootCache lootCache = getLootCache(String.valueOf(teamProfile.getId()));
            lootCache.addLocation(finalLocation);
            return;
        }

        LootCache lootCache = getLootCache(profile.getIdentifier());
        lootCache.addLocation(finalLocation);
    }

    private Location normalizeLocation(Location location) {
        location.setX(Math.floor(location.getX()));
        location.setY(Math.floor(location.getY()));
        location.setZ(Math.floor(location.getZ()));
        location.setPitch(0);
        location.setYaw(0);
        location.setHeadYaw(0);
        return location;
    }

    public static final class LootCache {
        private final Map<Location, Long> locationMap = new HashMap<>();

        public void addLocation(Location location) {
            locationMap.put(location, System.currentTimeMillis() + 20 * 1000L);
        }

        public void removeLocation(Location location) {
            locationMap.remove(location);
        }

        public boolean isValidLocation(Location location) {
            return locationMap.containsKey(location) && locationMap.get(location) >= System.currentTimeMillis();
        }

    }

}
