package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;
import alexis.practice.profile.settings.util.Colors;

import java.util.HashMap;
import java.util.Map;

public class PotionColor extends Setting {
    private final Map<String, Integer> currentColor = new HashMap<>();
    private final Map<String, Colors.RGB> color = new HashMap<>();

    public PotionColor(String name, boolean enabled) {
        super(name, enabled);
    }

    public PotionColor(String name) {
        super(name);
    }

    public void setCurrentColor(Profile profile, int color) {
        currentColor.put(profile.getIdentifier(), color);
        this.color.put(profile.getIdentifier(), Colors.potionColors.get(color));
    }

    public Colors.RGB getColorRGB(Profile profile) {
        return color.getOrDefault(profile.getIdentifier(), Colors.potionColors.get(0));
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