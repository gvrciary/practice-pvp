package alexis.practice.profile.data;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.SettingType;
import cn.nukkit.Player;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class SettingsData {

    private final Profile profile;
    private final Map<String, Boolean> settings = new HashMap<>((int) Arrays.stream(SettingType.values()).count());

    public SettingsData(Profile profile) {
        this.profile = profile;

        Arrays.stream(SettingType.values()).forEach(type -> settings.put(type.toString(), type.getSetting().isDefaultEnabled()));
    }

    public boolean isEnabled(String setting) {
        return settings.get(setting);
    }

    public void setEnabled(String setting, boolean value) {
        settings.put(setting, value);

        SettingType.get(setting).getSetting().execute(profile, value);
    }

    public void executeSetting(String setting, Player player) {
        SettingType.get(setting).getSetting().add(profile, player);
    }

    public void toggleEnabled(String setting) {
        setEnabled(setting, !isEnabled(setting));
    }

}
