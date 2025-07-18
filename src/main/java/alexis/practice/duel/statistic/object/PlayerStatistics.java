package alexis.practice.duel.statistic.object;

import alexis.practice.duel.Duel;
import alexis.practice.profile.Profile;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.item.ItemID;
import lombok.Getter;

@Getter
public final class PlayerStatistics {

    private int hits;
    private int combo;
    private int maxCombo;
    private float damage = 0.0f;

    private final Profile profile;
    private final Duel duel;

    public PlayerStatistics(Profile profile, Duel duel) {
        this.profile = profile;
        this.duel = duel;
    }

    public float getDamage() {
        return Math.round(damage * 100.0f) / 100.0f;
    }

    public void setHit(float damage) {
        hits++;
        combo++;

        this.damage += damage;

        if (maxCombo < combo) {
            maxCombo = combo;
        }
    }

    public void resetCombo() {
        combo = 0;
    }

    public void resetHits() {
        hits = 0;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append("&6").append(profile.getName()).append(" Statistic\n");

        this.hits = (duel.isBestOf() && duel.getKit().getName().equals("boxing")) ? hits * duel.getLimit() : hits;

        result.append("&fHits:&6 ").append(hits).append("\n");
        result.append("&fMax Combo:&6 ").append(maxCombo).append("\n");
        result.append("&fDamage Dealt:&6 ").append(getDamage()).append("\n");

        try {
            Player player = profile.getPlayer();

            if ("nodebuff".equals(duel.getKit().getName())) {
                result.append("&fPots: &6").append(Utils.countItems(player.getInventory().getContents().values(), ItemID.SPLASH_POTION));
            } else if ("hg".equals(duel.getKit().getName())) {
                result.append("&fSoups: &6").append(Utils.countItems(player.getInventory().getContents().values(), ItemID.MUSHROOM_STEW));
            }
        } catch (Exception ignored) {}

        return result.toString();
    }
}
