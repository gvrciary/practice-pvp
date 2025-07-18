package alexis.practice.profile.data;

import alexis.practice.Practice;
import alexis.practice.division.DivisionManager;
import alexis.practice.event.games.EventType;
import alexis.practice.profile.Profile;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class StatisticsData {

    private final Profile profile;

    private int matches = 0;
    private int deaths = 0;

    private int kills = 0;
    private int killStreak = 0;
    private int highestKillStreak = 0;

    private int wins = 0;
    private int winStreak = 0;
    private int highestWinStreak = 0;

    private int events = 0;

    public Map<String, Integer> eventsWins = new HashMap<>((int) Arrays.stream(EventType.values()).count());

    public StatisticsData(Profile profile) {
        this.profile = profile;
    }

    public int getElo(String type) {
        return profile.getKitData().getData(type).getElo();
    }

    public int getMatchesLost() {
        return matches - wins;
    }

    public int getEventsWins() {
        return eventsWins.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getMWRate() {
        double mwRate = (getMatchesLost() != 0 && wins != 0) ? (double) wins / getMatchesLost() : 0;
        return Math.round(mwRate * 100.0) / 100.0;
    }

    public double getKDRate() {
        double kdRate = (deaths != 0 && kills != 0) ? (double) kills / deaths : 0;
        return Math.round(kdRate * 100.0) / 100.0;
    }

    public void updateElo(String type, int newElo) {
        profile.getKitData().getData(type).setElo(newElo);
        DivisionManager.getInstance().update(profile, newElo);
    }

    public void setEventWins(String type, int newWins) {
        eventsWins.put(type, newWins);
    }

    public void increaseEvents() {
        events++;
    }

    public void increaseEventWin(String type) {
        eventsWins.put(type, getEventWin(type) + 1);
    }

    public int getEventWin(String type) {
        return eventsWins.getOrDefault(type, 0);
    }

    public void increaseKills() {
        kills++;

        if (kills > killStreak) {
            killStreak++;

            if (killStreak > highestKillStreak) {
                highestKillStreak++;

                if (highestKillStreak % 10 == 0){
                    Practice.getInstance().getServer().broadcastMessage(TextFormat.colorize("&a" + profile.getName() + " he has a streak of " + highestKillStreak + " kills"));
                }
            }
        }
    }

    public void increaseWins() {
        wins++;

        if (wins > winStreak) {
            winStreak++;

            if (winStreak > highestWinStreak) {
                highestWinStreak++;

                if (highestWinStreak % 10 == 0){
                    Practice.getInstance().getServer().broadcastMessage(TextFormat.colorize("&a" + profile.getName() + " he has a streak of " + highestKillStreak + " wins"));
                }
            }
        }
    }

    public void increaseDeaths() {
        deaths++;

        killStreak = 0;

        if (profile.getProfileData().getDuel() != null) {
            winStreak = 0;
        }
    }

    public void increaseMatches() {
        matches++;
    }
}
