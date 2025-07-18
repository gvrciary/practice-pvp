package alexis.practice.profile.settings;

import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Setting {
    protected final String name;
    protected boolean defaultEnabled;

    public Setting(String name) {
        this(name, true);
    }

    public void add(Profile profile, Player player) {}

    public void execute(Profile profile, boolean value) {}

    public void clearCache(Profile profile) {}

}