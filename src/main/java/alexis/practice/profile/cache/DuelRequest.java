package alexis.practice.profile.cache;

import alexis.practice.duel.DuelType;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import lombok.Getter;

@Getter
public class DuelRequest {
    private static final long TIME_EXPIRE = 2 * 60 * 1000;

    private final String identifier;
    private final long expire;

    private final DuelWorld worldData;
    private final DuelType type;
    private final int rounds;

    public DuelRequest(String identifier, DuelType type, int limit, DuelWorld worldData) {
        this.identifier = identifier;
        this.type = type;
        this.rounds = limit;
        this.worldData = worldData;

        this.expire = System.currentTimeMillis() + TIME_EXPIRE;
    }

    public boolean isValid() {
        Profile profile = getProfile();

        if (profile == null) return false;

        if (!profile.isOnline()) return false;

        if (!profile.getProfileData().isInLobby() || profile.getProfileData().getQueue() != null) return false;

        return expire > System.currentTimeMillis();
    }

    public Profile getProfile() {
        return ProfileManager.getInstance().get(identifier);
    }
}
