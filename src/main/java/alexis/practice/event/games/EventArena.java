package alexis.practice.event.games;

import alexis.practice.Practice;
import alexis.practice.event.Event;
import alexis.practice.event.EventManager;
import alexis.practice.event.games.types.tournament.Tournament;
import alexis.practice.event.team.Team;
import alexis.practice.event.world.EventWord;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Fireworks;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import alexis.practice.util.world.DeleteWorldAsync;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class EventArena {
    protected final int id;
    protected EventState currentState = EventState.WAITING;

    protected final Event event;

    protected int startTime = 5;
    protected int runTime = 0;
    protected int endTime = 5;

    @Setter
    protected int waitingTime = 90;
    @Setter
    protected boolean canDropItem = false;

    protected Level world;
    protected EventWord worldData;

    protected final Map<String, Integer> killsCache = new HashMap<>();

    protected final List<String> blockData = new ArrayList<>();
    protected final List<String> spectators = new ArrayList<>();

    public EventArena(int id, Event event, EventWord worldData, Level world) {
        this.id = id;
        this.event = event;
        this.worldData = worldData;
        this.world = world;
    }

    public EventArena(int id, Event event) {
        this.id = id;
        this.event = event;
    }

    public void tick() {
        switch (currentState) {
            case WAITING -> {
                if (getPlayers().size() <= 1) return;

                if (waitingTime > 0 && waitingTime < 90 && waitingTime % 10 == 0) event.sendMessage();

                if (waitingTime == 0) {
                    start();
                    return;
                }

                waitingTime--;

                if (waitingTime <= 5) {
                    event.broadcast("&6" + event.getType().getName() + "&f it will start in " + waitingTime + "s");
                    getPlayers().forEach(profile -> {
                        try {
                            PlayerUtil.playSound(profile.getPlayer(), "random.click");
                        } catch (Exception ignored) {}
                    });
                }

            }

            case STARTING -> startTime--;
            case RUNNING -> {
                if (event.isTeam() && runTime % 2 == 0) {
                    event.getTeamManager().getTeamsAlive().forEach(Team::setNameTags);
                }

                runTime++;
            }
            case ENDING -> {
                if (--endTime == 0) {
                    getSpectators().stream().filter(profile -> profile.getProfileData().isSpectator()).forEach(spectator -> spectator.getProfileData().setSpectate());

                    event.stop();
                }
            }
        }
    }

    public int getKills(Profile profile) {
        return killsCache.getOrDefault(profile.getIdentifier(), 0);
    }

    public void increaseKills(Profile profile) {
        killsCache.put(profile.getIdentifier(), getKills(profile) + 1);
    }

    public void addPlayer(Profile profile) {
        if (event.getType().equals(EventType.SUMO) ) {
            try {
                profile.getPlayer().teleport(world.getBlock(worldData.getLobbyPosition()));
            } catch (Exception ignored) {}
        }
    }

    public void start() {
        getPlayers().forEach(profile -> {
            profile.clear();
            profile.getCacheData().clearCooldown();
            profile.getCacheData().getCombat().clear();

            profile.getStatisticsData().increaseEvents();

            try {
                PlayerUtil.playSound(profile.getPlayer(), "random.pop");
            } catch (Exception ignored) {}
        });
    }

    public void removeBlock(Block block) {
        blockData.remove(block.getLocation().toString());
    }

    public boolean isBlock(Block block) {
        Location blockPosition = block.getLocation();
        return (world != null && blockPosition.getLevel().getFolderName().equals(world.getFolderName())) &&
                blockData.contains(blockPosition.toString());
    }

    public void addBlock(Block block) {
        blockData.add(block.getLocation().toString());
    }

    public boolean isSpectator(Profile profile) {
        return spectators.contains(profile.getIdentifier());
    }

    public void addSpectator(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();
            profile.clear();
            profile.getCacheData().getCombat().clear();
            profile.getCacheData().clearCooldown();

            if (event.getType().equals(EventType.SUMO)) {
                player.setGamemode(Player.ADVENTURE);
                player.teleport(world.getBlock(worldData.getLobbyPosition()));
            } else if (event.getType().equals(EventType.MEETUP)) {
                player.setGamemode(Player.SPECTATOR);
                player.teleport(world.getBlock(0, world.getHighestBlockAt(0, 0) + 1,0).getLocation());
            } else if (event.getType().equals(EventType.SKYWARS)) {
                Vector3 spawn = worldData.getSpawns().get(Utils.randomInteger(0, worldData.getSpawns().size() - 1));
                player.setGamemode(Player.SPECTATOR);
                player.teleport(world.getBlock(spawn));
            } else if (event.getType().equals(EventType.TOURNAMENT) && this instanceof Tournament tournament) {
                player.setGamemode(Player.SPECTATOR);
                tournament.setRandomSpectate(profile);
            }

            spectators.add(profile.getIdentifier());

            if (profile.getProfileData().getEvent() == null) {
                profile.getProfileData().setSpectate(this);
                player.sendMessage(TextFormat.colorize("&aType /hub to exit spectator mode"));
                event.broadcast("&a" + profile.getName() + " has joined as a spectator");

                player.getLevel().getPlayers().values().forEach(player::showPlayer);
            }
        } catch (Exception ignored) {}
    }

    public void removeSpectator(Profile profile) {
        spectators.remove(profile.getIdentifier());
        event.broadcast("&c" + profile.getName() + " has logged out as a spectator");

        if (profile.getProfileData().getEvent() == null) {
            profile.getProfileData().setSpectate();

            if (this instanceof Tournament tournament) {
                tournament.removeSpectate(profile);
            }
        }
    }

    public boolean isAlive(Profile profile) {
        return getPlayers().contains(profile);
    }

    public List<Profile> getPlayers() {
        return event.getPlayers().stream().filter(profile -> !isSpectator(profile)).toList();
    }

    public void checkWinner() {}

    public void stop() {
        currentState = EventState.ENDING;

        getPlayers().forEach(profile -> {
            profile.clear();
            profile.getCacheData().getCombat().clear();
            profile.getCacheData().clearCooldown();
        });

        if (event.isTeam() && event.getTeamManager().getTeamsAlive().size() == 1) {
            Optional<Team> teamWinner = event.getTeamManager().getTeamsAlive().stream().findAny();

            if (teamWinner.isPresent()) {
                Team team = teamWinner.get();

                team.getMembers().forEach(firstWinner -> {
                    firstWinner.getStatisticsData().increaseEventWin(event.getType().getName());

                    if (firstWinner.isOnline()) {
                        try {
                            Fireworks.spawnFirework(firstWinner.getPlayer());
                            PlayerUtil.playSound(firstWinner.getPlayer(), "random.levelup");
                        } catch (Exception ignored) {}
                    }
                });

                Practice.getInstance().getServer().broadcastMessage(TextFormat.colorize("&7Congratulations&6 Team #" + team.getId() + " &7for winning " + event.getType().getName()));
            }
        } else if (!event.isTeam() && getPlayers().size() == 1) {
            Optional<Profile> winner = getPlayers().stream().findAny();

            if (winner.isPresent()) {
                Profile profile = winner.get();

                profile.getStatisticsData().increaseEventWin(event.getType().getName());

                if (profile.isOnline()) {
                    try {
                        Player player = profile.getPlayer();
                        Fireworks.spawnFirework(player);
                        PlayerUtil.playSound(player, "random.levelup");
                    } catch (Exception ignored) {}
                }

                Practice.getInstance().getServer().broadcastMessage(TextFormat.colorize("&7Congratulations&6 " + profile.getName() + " &7for winning " + event.getType().getName()));
            }
        }
    }

    public List<Profile> getSpectators() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.spectators.contains(profile.getIdentifier()) &&
                        profile.isOnline())
                .collect(Collectors.toList());
    }

    public List<String> scoreboard(Profile profile) {
        return new ArrayList<>();
    }

    public void destroy() {}

    public void delete() {
        EventManager.getInstance().removeEvent(id);

        if (worldData == null) return;

        String worldName = world.getName();
        String path = Practice.getInstance().getServer().getDataPath() + File.separator + "worlds";

        Practice.getInstance().getServer().unloadLevel(world);
        Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new DeleteWorldAsync(worldName, path));
    }

}
