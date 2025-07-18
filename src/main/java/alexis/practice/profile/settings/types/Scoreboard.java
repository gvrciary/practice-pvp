package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;

public class Scoreboard extends Setting {

    public Scoreboard(String name, boolean enabled) {
        super(name, enabled);
    }

    public Scoreboard(String name) {
        super(name);
    }

    @Override
    public void execute(Profile profile, boolean value) {
        if (value) {
            profile.getScoreboard().spawn();
            return;
        }

        profile.getScoreboard().despawn();
    }

}
