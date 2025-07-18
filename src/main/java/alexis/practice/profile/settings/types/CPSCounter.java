package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPSCounter extends Setting {

    private final Map<String, List<Long>> cache = new HashMap<>();

    public CPSCounter(String name, boolean enabled) {
        super(name, enabled);
    }

    public CPSCounter(String name) {
        super(name);
    }

    public void add(Profile profile) {
        cache.computeIfAbsent(profile.getIdentifier(), k -> new ArrayList<>()).add(System.currentTimeMillis());
    }

    public int get(Profile profile) {
        if (!cache.containsKey(profile.getIdentifier())) return 0;

        cache.get(profile.getIdentifier()).removeIf(cps -> cps < System.currentTimeMillis() - 1000L);
        return cache.get(profile.getIdentifier()).size();
    }

    @Override
    public void clearCache(Profile profile) {
        cache.remove(profile.getIdentifier());
    }

}
