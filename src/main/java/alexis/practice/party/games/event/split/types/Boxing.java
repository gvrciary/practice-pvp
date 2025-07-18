package alexis.practice.party.games.event.split.types;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.party.games.event.split.PartySplit;
import alexis.practice.profile.Profile;
import cn.nukkit.level.Level;

import java.util.ArrayList;
import java.util.List;

public class Boxing extends PartySplit {

    private int firstHits = 0, secondHits = 0;
    private int firstCombo = 0, secondCombo = 0, firstMaxCombo = 0, secondMaxCombo = 0;

    public Boxing(int id, Party party, DuelWorld worldData, Level level, Kit kit) {
        super(id, party, worldData, level, kit);
    }

    public int getHits(Profile profile) {
        return (isFirstTeam(profile) ? firstHits : secondHits);
    }

    public int getCombo(Profile profile) {
        return (isFirstTeam(profile) ? firstCombo : secondCombo);
    }

    public void setHit(Profile profile) {
        if (isFirstTeam(profile)) {
            firstHits++;
            firstCombo++;

            if (firstHits >= 300) {
                stop(blueTeam);
                return;
            }

            if (firstMaxCombo < firstCombo) {
                firstMaxCombo++;
            }

            secondCombo = 0;
            return;
        }

        secondHits++;
        secondCombo++;

        if (secondHits >= 300) {
            stop(redTeam);
            return;
        }

        if (secondMaxCombo < secondCombo) {
            secondMaxCombo++;
        }

        firstCombo = 0;
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();
        if (currentState.equals(PartyGameState.RUNNING)) {
            if (isSpectator(profile) && profile.getProfileData().isSpectator()) {
                super.scoreboard(profile);
            }

            int yourHits = getHits(profile);
            int opponentHits = isFirstTeam(profile) ? secondHits : firstHits;
            int yourCombo = getCombo(profile);
            int hits = yourHits - opponentHits;

            lines.add("&fHits: " + (hits >= 0 ? "&a(+" + hits + ")" : "&c(" + hits + ")"));
            lines.add(" &fYour Team Hits: &6" + yourHits);
            lines.add(" &fThem Team Hits: &6" + opponentHits);
            lines.add(" " + (yourCombo == 0 ? "&cNo Combo" : "&a" + yourCombo + " Combo"));
            lines.add("&r&f");
            lines.add("&fYour Team: &6" + getAlivesTeam(isFirstTeam(profile) ? blueTeam : redTeam));
            lines.add("&fTheir Team: &6" + getAlivesTeam(isFirstTeam(profile) ? redTeam : blueTeam));
            return lines;
        }

        return super.scoreboard(profile);
    }

}
