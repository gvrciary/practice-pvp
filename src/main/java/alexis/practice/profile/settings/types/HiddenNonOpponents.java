package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;

import java.util.HashMap;
import java.util.Map;

public class HiddenNonOpponents extends Setting {
    private final Map<String, Boolean> isHidden = new HashMap<>();

    public HiddenNonOpponents(String name, boolean enabled) {
        super(name, enabled);
    }

    public HiddenNonOpponents(String name) {
        super(name);
    }

    public boolean isHidden(Profile profile) {
        return isHidden.getOrDefault(profile.getIdentifier(), false);
    }

    public void setHidden(Profile profile, boolean value) {
        isHidden.put(profile.getIdentifier(), value);
    }

    @Override
    public void clearCache(Profile profile) {
        isHidden.remove(profile.getIdentifier());
    }

}
