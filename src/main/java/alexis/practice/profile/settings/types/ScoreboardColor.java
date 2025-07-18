package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;
import alexis.practice.profile.settings.util.Colors;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardColor extends Setting {
    private final Map<String, Integer> currentColor = new HashMap<>();
    private final Map<String, String> color = new HashMap<>();

    public ScoreboardColor(String name, boolean enabled) {
        super(name, enabled);
    }

    public ScoreboardColor(String name) {
        super(name);
    }

    public void setCurrentColor(Profile profile, int color) {
        currentColor.put(profile.getIdentifier(), color);
        this.color.put(profile.getIdentifier(), Colors.scoreboardColors.get(color).substring(0, 2));
    }

    public String getColor(Profile profile) {
        return color.getOrDefault(profile.getIdentifier(), Colors.scoreboardColors.get(0).substring(0, 2));
    }

    public int getCurrentColor(Profile profile) {
        return currentColor.getOrDefault(profile.getIdentifier(), 0);
    }

    @Override
    public void clearCache(Profile profile) {
        currentColor.remove(profile.getIdentifier());
        color.remove(profile.getIdentifier());
    }

}