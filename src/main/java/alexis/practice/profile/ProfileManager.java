package alexis.practice.profile;

import cn.nukkit.Player;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public final class ProfileManager {
    @Getter
    private static final ProfileManager instance = new ProfileManager();
    @Getter
    private final ConcurrentHashMap<String, Profile> profiles = new ConcurrentHashMap<>();

    public Profile get(Player player) {
        return profiles.get(player.getLoginChainData().getXUID());
    }

    public Profile get(String id) {
        return profiles.get(id);
    }

    public void create(Player player) {
        Profile profile = new Profile(player);
        profiles.put(player.getLoginChainData().getXUID(), profile);
    }

    public void remove(Profile profile) {
        profiles.remove(profile.getIdentifier());
    }

}