package alexis.practice.party.games.duel.types;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.party.games.duel.PartyDuel;
import alexis.practice.profile.Profile;
import cn.nukkit.level.Level;

import java.util.ArrayList;
import java.util.List;

public class Boxing extends PartyDuel {

    private int firstHits = 0, secondHits = 0;
    private int firstCombo = 0, secondCombo = 0, firstMaxCombo = 0, secondMaxCombo = 0;

    public Boxing(int id, Party firstTeam, Party secondTeam, DuelWorld worldData, Level level, Kit kit) {
        super(id, firstTeam, secondTeam, worldData, level, kit);
    }

    public int getHits(Party party) {
        return (isFirstParty(party) ? firstHits : secondHits);
    }

    public int getCombo(Party party) {
        return (isFirstParty(party) ? firstCombo : secondCombo);
    }

    public void setHit(Party party) {
        if (isFirstParty(party)) {
            firstHits++;
            firstCombo++;

            if (firstHits >= 300) {
                stop(party);
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
            stop(party);
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

            Party party = profile.getProfileData().getParty();
            Party opponent = getOpponentParty(party);

            int yourHits = getHits(party);
            int opponentHits = getHits(opponent);
            int yourCombo = getCombo(party);
            int hits = yourHits - opponentHits;

            lines.add("&fHits: " + (hits >= 0 ? "&a(+" + hits + ")" : "&c(" + hits + ")"));
            lines.add(" &fYour Team Hits: &6" + yourHits);
            lines.add(" &fThem Team Hits: &6" + opponentHits);
            lines.add(" " + (yourCombo == 0 ? "&cNo Combo" : "&a" + yourCombo + " Combo"));
            lines.add("&r&f");
            lines.add("&fYour Team: &6" + getPartyAlive(party));
            lines.add("&fTheir Team: &6" + getPartyAlive(opponent));
            return lines;
        }

        return super.scoreboard(profile);
    }

}
