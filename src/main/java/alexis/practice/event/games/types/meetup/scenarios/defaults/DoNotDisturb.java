package alexis.practice.event.games.types.meetup.scenarios.defaults;

import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import alexis.practice.event.team.Team;
import alexis.practice.profile.Profile;
import cn.nukkit.item.Item;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DoNotDisturb extends Scenario {

    private static final int TIME = 15;

    private final Map<String, Data> cache = new HashMap<>();

    public DoNotDisturb(Meetup eventArena, String name, String description, Item item, boolean enabled) {
        super(eventArena, name, description, item, enabled);
    }

    public boolean inData(Profile profile) {
        Data data = getData(profile);

        if (data != null && data.getTime() < System.currentTimeMillis()) {
            removeData(profile);
        }

        return data != null && data.getTime() > System.currentTimeMillis();
    }

    public Data getData(Profile profile) {
        if (eventArena.getEvent().isTeam()) {
            Team teamProfile = eventArena.getEvent().getTeamManager().getTeam(profile);

            return cache.get(String.valueOf(teamProfile.getId()));
        }

        return cache.get(profile.getIdentifier());
    }

    public void removeData(Profile profile) {
        if (eventArena.getEvent().isTeam()) {
            Team teamProfile = eventArena.getEvent().getTeamManager().getTeam(profile);

            cache.remove(String.valueOf(teamProfile.getId()));
            return;
        }

        cache.remove(profile.getIdentifier());
    }

    public Long getTime(Profile profile) {
        if (eventArena.getEvent().isTeam()) {
            Team teamProfile = eventArena.getEvent().getTeamManager().getTeam(profile);

            return cache.get(String.valueOf(teamProfile.getId())).getTime();
        }

        return cache.get(profile.getIdentifier()).getTime();
    }

    public void setDND(Profile profile, Profile target) {
        if (eventArena.getEvent().isTeam()) {
            Team teamProfile = eventArena.getEvent().getTeamManager().getTeam(profile);
            Team teamTarget = eventArena.getEvent().getTeamManager().getTeam(target);

            if (teamProfile == null || teamTarget == null) {
                return;
            }

            cache.put(String.valueOf(teamProfile.getId()), new Data(String.valueOf(teamTarget.getId())));
            cache.put(String.valueOf(teamTarget.getId()), new Data(String.valueOf(teamProfile.getId())));
            return;
        }

        cache.put(profile.getIdentifier(), new Data(target.getIdentifier()));
        cache.put(target.getIdentifier(), new Data(profile.getIdentifier()));
    }


    public static final class Data {
        @Getter
        private final String enemy;
        private final long time;

        public Data(String enemy) {
            this.enemy = enemy;
            this.time = System.currentTimeMillis() + TIME * 1000L;
        }

        public Long getTime() {
            return time;
        }
    }

}
