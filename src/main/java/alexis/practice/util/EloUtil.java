package alexis.practice.util;

import alexis.practice.duel.DuelType;
import alexis.practice.profile.Profile;
import cn.nukkit.utils.TextFormat;

public class EloUtil {

    private static final KFactor[] K_FACTORS = {
            new KFactor(0, 1000, 25),
            new KFactor(1001, 1400, 20),
            new KFactor(1401, 1800, 15),
            new KFactor(1801, 2200, 10)
    };

    private static final int DEFAULT_K_FACTOR = 25;
    private static final int WIN = 1;
    private static final int LOSS = 0;

    public static void setElo(Profile victim, Profile target, DuelType type) {
        int eloWinner = target.getStatisticsData().getElo(type.getName());
        int eloLoser = victim.getStatisticsData().getElo(type.getName());

        int newWinnerElo = EloUtil.getNewRating(eloWinner, eloLoser, true);
        int newLoserElo = EloUtil.getNewRating(eloLoser, eloWinner, false);

        int eloDifWinner = newWinnerElo - eloWinner;
        int eloDifLoser = eloLoser - newLoserElo;

        target.getStatisticsData().updateElo(type.getName(), newWinnerElo);
        victim.getStatisticsData().updateElo(type.getName(), newLoserElo);

        try {
            if (target.isOnline()) {
                target.getPlayer().sendMessage(TextFormat.colorize("&l&6Elo Updates:\n&r&a" + target.getName() + " " + target.getStatisticsData().getElo(type.getName()) + " (+" + eloDifWinner + ")" + "\n&r&c"+ victim.getName() + " " + victim.getStatisticsData().getElo(type.getName()) + " (-" + eloDifLoser + ")"));
            }

            if (victim.isOnline()){
                victim.getPlayer().sendMessage(TextFormat.colorize("&l&6Elo Updates:\n&r&a" + target.getName() + " " + target.getStatisticsData().getElo(type.getName()) + " (+" + eloDifWinner + ")" + "\n&r&c"+ victim.getName() + " " + victim.getStatisticsData().getElo(type.getName()) + " (-" + eloDifLoser + ")"));
            }
        } catch (Exception ignored) {}
    }

    private static int getNewRating(int rating, int opponentRating, boolean won) {
        if (won) {
            return EloUtil.getNewRating(rating, opponentRating, EloUtil.WIN);
        } else {
            return EloUtil.getNewRating(rating, opponentRating, EloUtil.LOSS);
        }
    }

    private static int getNewRating(int rating, int opponentRating, int score) {
        double kFactor = EloUtil.getKFactor(rating);
        double expectedScore = EloUtil.getExpectedScore(rating, opponentRating);
        int newRating = EloUtil.calculateNewRating(rating, score, expectedScore, kFactor);

        if (score == 1) {
            if (newRating == rating) {
                newRating++;
            }
        }

        return newRating;
    }

    private static int calculateNewRating(int oldRating, int score, double expectedScore, double kFactor) {
        return oldRating + (int) (kFactor * (score - expectedScore));
    }

    private static double getKFactor(int rating) {
        for (int i = 0; i < EloUtil.K_FACTORS.length; i++) {
            if (rating >= EloUtil.K_FACTORS[i].startIndex() && rating <= EloUtil.K_FACTORS[i].endIndex()) {
                return EloUtil.K_FACTORS[i].value();
            }
        }

        return EloUtil.DEFAULT_K_FACTOR;
    }

    private static double getExpectedScore(int rating, int opponentRating) {
        return 1 / (1 + Math.pow(10, ((double) (opponentRating - rating) / 400)));
    }

    private record KFactor(int startIndex, int endIndex, double value) {
        @Override
        public int startIndex() {
            return startIndex;
        }

        @Override
        public int endIndex() {
            return endIndex;
        }

        @Override
        public double value() {
            return value;
        }
    }

}
