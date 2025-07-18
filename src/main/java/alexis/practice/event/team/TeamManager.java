package alexis.practice.event.team;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventState;
import alexis.practice.item.HotbarItem;
import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.window.SimpleWindowForm;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Getter
public class TeamManager {

    private final Event event;
    private final Map<Integer, Team> teams = new HashMap<>();

    public TeamManager(Event event, int size) {
        int teamCounts = switch (size) {
            case 2 -> 25;
            case 3 -> 17;
            case 4 -> 13;
            case 5 -> 10;
            default -> 2;
        };

        int newMaxPlayers = switch (size) {
            case 3 -> 51;
            case 4 -> 52;
            default -> 50;
        };

        this.event = event;

        event.setMaxPlayers(newMaxPlayers);
        IntStream.range(0, teamCounts)
                .forEach(i -> teams.computeIfAbsent(i, id -> new Team(id, event, size)));
    }

    @Nullable
    public Team getTeam(Profile profile) {
        return teams.values().stream()
                .filter(team -> team.isMember(profile))
                .findFirst()
                .orElse(null);
    }

    public List<Team> getTeamsAlive() {
        return teams.values().stream().filter(Team::isAlive).toList();
    }

    public void setInRandomTeam(Profile profile) {
        List<Team> availableTeams = teams.values().stream()
                .filter(team -> !team.isFull())
                .toList();

        if (!availableTeams.isEmpty()) {
            Team firstTeam = availableTeams.get(0);
            firstTeam.addMember(profile);
            return;
        }

        event.getEventArena().addSpectator(profile);
    }

    public void sendTeamForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("team_selector", "Team Selector")
                .addHandler(h -> {
                    if (!h.isFormValid("team_selector")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        int id = Integer.parseInt(button.getName());
                        Team team = teams.get(id);

                        if (team == null) return;

                        if (this.event.getEventArena() != null && !this.event.getEventArena().getCurrentState().equals(EventState.WAITING)) return;

                        if (team.isFull()) {
                            player.sendMessage(TextFormat.colorize("&cTeam is full"));
                            return;
                        }

                        team.addMember(profile);
                        player.getInventory().setItem(4, HotbarItem.TEAM_INFO.getItem());
                        player.sendMessage(TextFormat.colorize("&aYou have joined the team #" + team.getId()));
                    }
                });
        teams.values().forEach(team -> form.addButton(String.valueOf(team.getId()), "Team #" + team.getId() + "\nMembers: " + team.getMembersAlive().size()));

        form.sendTo(player);
    }

}
