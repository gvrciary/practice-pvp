package alexis.practice.event.games.types.tournament;

import alexis.practice.Practice;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.duel.world.DuelWorldManager;
import alexis.practice.event.Event;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.games.EventState;
import alexis.practice.event.games.types.tournament.match.Match;
import alexis.practice.event.games.types.tournament.match.MatchState;
import alexis.practice.event.team.Team;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tournament extends EventArena {

    private final java.util.Queue<Profile> queues = new ConcurrentLinkedQueue<>();
    private final java.util.Queue<Team> queuesTeam = new ConcurrentLinkedQueue<>();

    private final ConcurrentHashMap<Integer, Match> matches = new ConcurrentHashMap<>();

    protected int countdownCombat = 5;

    private TournamentState state = TournamentState.ROUND_OF_16;

    public Tournament(int id, Event event) {
        super(id, event);
    }

    public void start() {
        super.start();
        assignFight();
        currentState = EventState.RUNNING;
    }

    private void assignFight() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (event.isTeam()) {
                getPlayers().forEach(profile -> {
                    Team team = event.getTeamManager().getTeam(profile);

                    if (team == null) {
                        event.getTeamManager().setInRandomTeam(profile);
                    }
                });

                int teams = event.getTeamManager().getTeamsAlive().size();

                if (teams <= 1){
                    stop();
                    return;
                } else if (teams == 2) {
                    state = TournamentState.FINALS;
                } else if (teams <= 4) {
                    state = TournamentState.SEMI_FINALS;
                } else if (teams <= 8) {
                    state = TournamentState.QUARTER_FINALS;
                } else if (teams <= 16) {
                    state = TournamentState.ROUND_OF_16;
                } else {
                    state = TournamentState.GROUP_STAGE;
                }

                event.getTeamManager().getTeamsAlive().forEach(team -> {
                    Optional<Team> opponentTeam = queuesTeam.stream().findAny();

                    if (opponentTeam.isPresent()) {
                        createDuelTeam(team, opponentTeam.get());
                    } else queuesTeam.add(team);
                });

                queuesTeam.forEach(team -> {
                    team.broadcast("&cYou were not assigned to any fight");
                    team.broadcast("&aWait for the next round");
                });

                queuesTeam.clear();
                countdownCombat = 5;
                return;
            }

            int players = getPlayers().size();

            if (players <= 1){
                stop();
                return;
            } else if (players == 2) {
                state = TournamentState.FINALS;
            } else if (players <= 4) {
                state = TournamentState.SEMI_FINALS;
            } else if (players <= 8) {
                state = TournamentState.QUARTER_FINALS;
            } else if (players <= 16) {
                state = TournamentState.ROUND_OF_16;
            } else {
                state = TournamentState.GROUP_STAGE;
            }

            getPlayers().forEach(profile -> {
                Optional<Profile> opponentProfile = queues.stream().findAny();

                if (opponentProfile.isPresent()) {
                    createDuel(profile, opponentProfile.get());
                } else queues.add(profile);
            });

            queues.forEach(profile -> {
                try {
                    Player player = profile.getPlayer();

                    player.sendMessage(TextFormat.colorize("&cYou were not assigned to any fight"));
                    player.sendMessage(TextFormat.colorize("&aWait for the next round"));
                } catch (Exception ignored) {}
            });

            queues.clear();
            countdownCombat = 5;
        });

        executor.shutdown();
    }

    public void checkWinner() {
        if (currentState.equals(EventState.ENDING)) return;

        if (event.isTeam()) {
            int teams = event.getTeamManager().getTeamsAlive().size();

            if (teams <= 1){
                stop();
            }

            return;
        }

        int players = getPlayers().size();

        if (players <= 1){
            stop();
        }
    }

    public void tick() {
        if (currentState == EventState.RUNNING) {
            super.tick();

            if (!matches.isEmpty()) {
                matches.values().forEach(Match::tick);
            } else {
                if (countdownCombat == 0) {
                    assignFight();
                    return;
                }

                if (countdownCombat < 5) {
                    event.broadcast("&6The next round will start in &f" + countdownCombat + "s");
                    getPlayers().forEach(profile -> {
                        try {
                            PlayerUtil.playSound(profile.getPlayer(), "random.click");
                        } catch (Exception ignored) {}
                    });
                }

                countdownCombat--;
            }

        } else super.tick();

    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        switch (currentState) {
            case WAITING -> {
                List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(Profile::isOnline).toList();
                lines.add("&l&6|&r &fOnline:&6 " + players.size());
                lines.add("&r&9");
                lines.add(" &l&6Tournament");
                lines.add("&l&6|&r &fMode:&6 " + (event.isTeam() ? "TO" + event.getCountTeam() : "FFA"));
                lines.add("&l&6|&r &fLadder:&6 " + event.getKit().getCustomName());
                lines.add("&l&6|&r &fPlayers:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                lines.add("&r&f");

                if (getPlayers().size() > 1) lines.add("&l&6|&r &fStart in:&6 " + Utils.formatTime(waitingTime));
            }

            case RUNNING -> {
                if (isSpectator(profile)) {
                    lines.add("&l&6|&r &fMode:&6 " + (event.isTeam() ? "TO" + event.getCountTeam() : "FFA"));
                    lines.add("&l&6|&r &fLadder:&6 " + event.getKit().getCustomName());
                    lines.add("&l&6|&r &fState:&6 " + state.getName());
                    lines.add("&l&6|&r &fRemaining:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                    break;
                }

                Match match = inAnyMatch(profile);

                if (match != null) {
                    return match.scoreboard(profile);
                } else {
                    lines.add("&l&6|&r &fWaiting for next duel");
                    lines.add("&l&6|&r &fState:&6 " + state.getName());
                    lines.add("&l&6|&r &fRemaining:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                }
            }

            case ENDING -> {
                if (!isSpectator(profile)) {
                    lines.add("&l&6|&a VICTORY");
                    lines.add("&6&r");
                }

                lines.add("&l&6|&r &cEvent ended");
            }
        }

        return lines;
    }

    public void removeSpectate(Profile profile) {
        matches.values().forEach(match -> match.removeSpectator(profile));
    }

    public void setRandomSpectate(Profile profile) {
        removeSpectate(profile);

        List<Match> matchs = new ArrayList<>(matches.values().stream().filter(match -> match.getState().equals(MatchState.RUNNING)).toList());

        if (matchs.isEmpty()) {
            try {
                Player player = profile.getPlayer();

                player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());
            } catch (Exception ignored) {}

            return;
        }

        Collections.shuffle(matchs);
        Optional<Match> findMatch = matchs.stream().findAny();
        findMatch.ifPresent(match -> match.addSpectator(profile));
    }

    @Nullable
    public Match inAnyMatch(Profile profile) {
       return matches.values().stream().filter(m -> m.inThisMatch(profile)).findAny().orElse(null);
    }

    private void createDuel(Profile firstProfile, Profile secondProfile) {
        Practice practice = Practice.getInstance();
        int id = 0;

        while (matches.containsKey(id) || new File(practice.getServer().getDataPath() + "worlds" + File.separator + "tournamentMatch-" + id).exists()) {
            id++;
        }

        DuelWorld worldData = DuelWorldManager.getInstance().getRandomWorld(event.getKit().getName());

        queues.remove(firstProfile);
        queues.remove(secondProfile);

        int finalId = id;
        worldData.copyWorld("tournamentMatch-" + id, practice.getServer().getDataPath() + File.separator + "worlds" + File.separator, () -> {
            try {
                Level world = practice.getServer().getLevelByName("tournamentMatch-" + finalId);
                Match match = new Match(this, finalId, firstProfile, secondProfile, world, worldData);
                matches.put(finalId, match);
            } catch (Exception e) {
                System.out.println("Error when creating the Tournament Match " + finalId);
            }
        });
    }

    private void createDuelTeam(Team firstTeam, Team secondTeam) {
        Practice practice = Practice.getInstance();
        int id = 0;

        while (matches.containsKey(id) || new File(practice.getServer().getDataPath() + "worlds" + File.separator + "tournamentMatch-" + id).exists()) {
            id++;
        }

        DuelWorld worldData = DuelWorldManager.getInstance().getRandomWorld(event.getKit().getName());

        queuesTeam.remove(firstTeam);
        queuesTeam.remove(secondTeam);

        int finalId = id;
        worldData.copyWorld("tournamentMatch-" + id, practice.getServer().getDataPath() + File.separator + "worlds" + File.separator, () -> {
            try {
                Level world = practice.getServer().getLevelByName("tournamentMatch-" + finalId);
                Match match = new Match(this, finalId, firstTeam, secondTeam, world, worldData);
                matches.put(finalId, match);
            } catch (Exception e) {
                System.out.println("Error when creating the Tournament Match " + finalId);
            }
        });
    }

    public void removeDuel(int id) {
        matches.remove(id);
    }

    @Override
    public void destroy() {
        matches.values().forEach(Match::stop);
    }

    @Override
    public void delete() {
        matches.values().forEach(Match::stop);
        super.delete();
    }

}
