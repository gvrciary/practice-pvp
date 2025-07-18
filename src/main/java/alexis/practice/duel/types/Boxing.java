package alexis.practice.duel.types;

import alexis.practice.duel.Duel;
import alexis.practice.duel.DuelState;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import cn.nukkit.level.Level;

import java.util.ArrayList;
import java.util.List;

public class Boxing extends Duel {

    public Boxing(int id, Profile firstProfile, Profile secondProfile, Level world, DuelWorld worldData, Kit kit, boolean ranked, int limit, boolean isDuel) {
        super(id, firstProfile, secondProfile, world, worldData, kit, ranked, limit, isDuel);
    }

    public void check(Profile profile) {
        if (duelStatistic.getStatistic(profile).getHits() >= 100) {
            super.setDeath(getOpponentProfile(profile));

            if (isBestOf() && getRounds(profile) < limit) {
                duelStatistic.getStatistic(firstProfile).resetHits();
                duelStatistic.getStatistic(secondProfile).resetHits();
            }
        }
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();
        if (state.equals(DuelState.RUNNING) && !isSpectator(profile)) {
            Profile opponent = getOpponentProfile(profile);

            int yourHits = duelStatistic.getStatistic(profile).getHits();
            int opponentHits = duelStatistic.getOpponentStatistic(profile).getHits();
            int yourCombo = duelStatistic.getStatistic(profile).getCombo();
            int hits = yourHits - opponentHits;

            lines.add("&l&6|&r &fHits: " + (hits >= 0 ? "&a(+" + hits + ")" : "&c(" + hits + ")"));
            lines.add(" &fYou: &6" + yourHits);
            lines.add(" &fThem: &6" + opponentHits);
            lines.add(" " + (yourCombo == 0 ? "&cNo Combo" : "&a" + yourCombo + " Combo"));
            lines.add("&r&f");
            try {
                lines.add("&l&6|&r &fYour ping: &6" + profile.getPlayer().getPing() + "ms");
                lines.add("&l&6|&r &fTheir ping: &6" + opponent.getPlayer().getPing() + "ms");
            } catch (Exception ignored) {}
            return lines;
        }

        return super.scoreboard(profile);
    }
}
