package alexis.practice.duel.statistic;

import alexis.practice.duel.Duel;
import alexis.practice.duel.statistic.object.PlayerStatistics;
import alexis.practice.profile.Profile;
import lombok.Getter;

public final class DuelStatistic {

    private final PlayerStatistics firstStatistic;
    private final PlayerStatistics secondStatistic;
    @Getter
    private final Duel duel;

    public DuelStatistic(Duel duel) {
        this.duel = duel;
        this.firstStatistic = new PlayerStatistics(this.duel.getFirstProfile(), duel);
        this.secondStatistic = new PlayerStatistics(this.duel.getSecondProfile(), duel);
    }

    public PlayerStatistics getStatistic(Profile profile) {
        if (profile.getIdentifier().equals(duel.getFirstProfile().getIdentifier())) {
            return firstStatistic;
        }

        return secondStatistic;
    }

    public PlayerStatistics getOpponentStatistic(Profile profile) {
        if (profile.getIdentifier().equals(duel.getFirstProfile().getIdentifier())) {
            return secondStatistic;
        }

        return firstStatistic;
    }

    public void setHit(Profile profile, float damage) {
        getStatistic(profile).setHit(damage);
        getOpponentStatistic(profile).resetCombo();
    }

    @Override
    public String toString() {
        return firstStatistic.toString() + "\n\n" + secondStatistic.toString();
    }

}