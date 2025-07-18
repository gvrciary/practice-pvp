package alexis.practice.queue;

import alexis.practice.duel.DuelType;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import lombok.Getter;

public class Queue {
    @Getter
    private final long time;
    @Getter
    private final String identifier;
    @Getter
    private final DuelType type;
    @Getter
    private final boolean ranked;
    @Getter
    private final boolean is2vs2;

    private int elo = 1000;
    private int range = 0;
    private long lastRangeIncreaseTime = System.currentTimeMillis();

    public Queue(Profile profile, boolean ranked, DuelType type, boolean is2vs2) {
        this.identifier = profile.getIdentifier();
        this.type = type;
        this.ranked = ranked;
        this.is2vs2 = is2vs2;

        time = System.currentTimeMillis();

        if (ranked) {
            elo = profile.getProfileData().getProfile().getStatisticsData().getElo(type.getName());
            range = 15;
        }

        profile.getProfileData().setQueue(this);
    }

    public boolean isCompatible(Queue profile) {
        return profile.getProfile() != null && getProfile() != null &&
                !profile.getProfile().getIdentifier().equals(getProfile().getIdentifier()) && profile.inQueue() && inQueue() &&
                profile.getType().equals(type) && profile.isRanked() == isRanked() && !is2vs2 && !profile.is2vs2() &&
                (!isRanked() || verifyRange(getEloMin(), getEloMax(), profile.getEloMin(), profile.getEloMax(), elo, profile.elo));
    }

    public boolean isCompatible2vs2(Queue profile) {
        return profile.getProfile() != null && getProfile() != null &&
                !profile.getProfile().getIdentifier().equals(getProfile().getIdentifier()) && profile.inQueue() && inQueue()
                && is2vs2 && profile.is2vs2 && profile.getType().equals(type);
    }

    public static boolean verifyRange(int min1, int max1, int min2, int max2, int num1, int num2) {
        return (num1 >= min1 && num1 <= max1) || (num2 >= min1 && num2 <= max1) || (num1 >= min2 && num1 <= max2) || (num2 >= min2 && num2 <= max2);
    }

    public boolean inQueue() {
        return getProfile() != null && getProfile().isOnline() && getProfile().getProfileData().getQueue() != null && getProfile().getProfileData().getDuel() == null && getProfile().getProfileData().getQueue().getType().equals(type);
    }

    public void increaseRange() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRangeIncreaseTime >= 3000L) {
            range += 15;
            lastRangeIncreaseTime = currentTime;
        }
    }

    public Profile getProfile() {
        return ProfileManager.getInstance().get(identifier);
    }

    public int getEloMax() {
        return Math.min(elo + range, 2500);
    }

    public int getEloMin() {
        return Math.max(elo - range, 0);
    }

}
