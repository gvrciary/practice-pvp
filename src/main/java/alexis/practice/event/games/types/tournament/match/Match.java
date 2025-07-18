package alexis.practice.event.games.types.tournament.match;

import alexis.practice.Practice;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.event.games.types.tournament.Tournament;
import alexis.practice.event.team.Team;
import alexis.practice.item.HotbarItem;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Fireworks;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.world.DeleteWorldAsync;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Match {
    protected final Tournament eventArena;
    protected int id;

    protected int startTime = 5;
    protected int runTime = 0;
    protected int endTime = 5;

    protected MatchState state = MatchState.STARTING;

    protected Profile firstProfile;
    protected Profile secondProfile;

    protected Team firstTeam;
    protected Team secondTeam;

    protected Level world;
    protected DuelWorld worldData;

    protected final List<String> blockData = new ArrayList<>();
    protected final List<String> spectators = new ArrayList<>();

    private static final int LIMIT_DURATION = 8 * 60;

    public Match(Tournament eventArena, int id, Profile firstProfile, Profile secondProfile, Level world, DuelWorld worldData) {
        this.eventArena = eventArena;
        this.id = id;
        this.firstProfile = firstProfile;
        this.secondProfile = secondProfile;

        this.world = world;
        this.worldData = worldData;

        setup();
    }

    public Match(Tournament eventArena, int id, Team firstTeam, Team secondTeam, Level world, DuelWorld worldData) {
        this.eventArena = eventArena;
        this.id = id;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;

        this.world = world;
        this.worldData = worldData;

        setup();
    }

    public void setup() {
        if (eventArena.getEvent().isTeam()) {
            if (!firstTeam.isAlive()|| !secondTeam.isAlive()) {
                stop();
                return;
            }

            getAllPlayers().forEach(profile -> {
                try {
                    Player player = profile.getPlayer();
                    player.setImmobile();
                    player.setMaxHealth(player.getMaxHealth());
                    Vector3 pos = (isFirstTeam(profile) ? worldData.getFirstPosition() : worldData.getSecondPosition());

                    player.teleport(world.getBlock(pos).getLocation());

                    profile.getCacheData().clearCooldown();
                    getKit(profile);
                } catch (Exception ignore) {}
            });

            return;
        }

        if (!firstProfile.isOnline() || !secondProfile.isOnline()) {
            stop();
            return;
        }

        try {
            if (firstProfile.isOnline()) {
                Player firstPlayer = firstProfile.getPlayer();
                firstPlayer.setImmobile();
                firstPlayer.setMaxHealth(firstPlayer.getMaxHealth());
                firstPlayer.teleport(world.getBlock(worldData.getFirstPosition()).getLocation());
                getKit(firstProfile);

                if (secondProfile.isOnline()) {
                    Player secondPlayer = secondProfile.getPlayer();

                    firstPlayer.showPlayer(secondPlayer);
                }
            }

            if (secondProfile.isOnline()) {
                Player secondPlayer = secondProfile.getPlayer();
                secondPlayer.setImmobile();
                secondPlayer.setMaxHealth(secondPlayer.getMaxHealth());
                secondPlayer.teleport(world.getBlock(worldData.getSecondPosition()).getLocation());
                getKit(secondProfile);

                if (firstProfile.isOnline()) {
                    Player firstPlayer = firstProfile.getPlayer();

                    secondPlayer.showPlayer(firstPlayer);
                }
            }
        } catch (Exception ignored) {}
    }

    public void tick() {
        switch (state) {
            case STARTING -> {
                if (startTime <= 0) {
                    start();
                    return;
                }

                broadcast("&6" + startTime + "...");

                if (eventArena.getEvent().isTeam()) {
                    getAllPlayers().forEach(profile -> {
                        try {
                            PlayerUtil.playSound(profile.getPlayer(), "random.click");
                        } catch (Exception ignored) {}
                    });
                } else {
                    if (firstProfile.isOnline()) {
                        try {
                            PlayerUtil.playSound(firstProfile.getPlayer(), "random.click");
                        } catch (Exception ignored) {}
                    }

                    if (secondProfile.isOnline()) {
                        try {
                            PlayerUtil.playSound(secondProfile.getPlayer(), "random.click");
                        } catch (Exception ignored) {}
                    }
                }

                startTime--;
            }

            case RUNNING -> {
                runTime++;

                if (LIMIT_DURATION == runTime) {
                    stop();
                }
            }
        }
    }

    public void start() {
        if (eventArena.getEvent().isTeam()) {
            if (!firstTeam.isAlive()|| !secondTeam.isAlive()) {
                stop();
                return;
            }

            state = MatchState.RUNNING;

            getAllPlayers().forEach(profile -> {
                try {
                    Player player = profile.getPlayer();
                    player.getLevel().getPlayers().values().forEach(player::showPlayer);
                    player.setImmobile(false);
                } catch (Exception ignore) {}
            });

            broadcast("&aMatch started");
            return;
        }

        if (!firstProfile.isOnline() || !secondProfile.isOnline()) {
            stop();
            return;
        }

        state = MatchState.RUNNING;

        try {
            if (firstProfile.isOnline()) {
                Player firstPlayer = firstProfile.getPlayer();
                firstPlayer.setImmobile(false);
            }

            if (secondProfile.isOnline()) {
                Player secondPlayer = secondProfile.getPlayer();
                secondPlayer.setImmobile(false);
            }
        } catch (Exception ignored) {}

        broadcast("&aMatch started");
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        if (eventArena.getEvent().isTeam()) {
            switch (state) {
                case STARTING, RUNNING -> {
                    Team opponents = getOpponentTeam(profile);
                    Team yourTeam = isFirstTeam(profile) ? firstTeam : secondTeam;

                    if (state.equals(MatchState.STARTING)) {
                        lines.add("&l&6|&r&f Opponents: ");
                        opponents.getMembersAlive().forEach(p -> {
                            if (!isValid(profile)) return;

                            try {
                                lines.add(" &r&f" + p.getName() + " &7(" + profile.getPlayer().getPing() + ")");
                            } catch (Exception ignored) {}
                        });

                        lines.add("&r&4");
                    }

                    try {
                        yourTeam.getMembersAlive().stream().filter(p -> !profile.getIdentifier().equals(p.getIdentifier())).forEach(p -> {
                            if (!isValid(profile)) return;

                            try {
                                lines.add(" &r&c" + p.getName() + " &7(" + profile.getPlayer().getPing() + ")");
                            } catch (Exception ignored) {}
                        });

                        if (state.equals(MatchState.RUNNING)) {
                            lines.add("&l&6|&r &fTheir Team: ");
                            opponents.getMembersAlive().forEach(p -> {
                                if (!isValid(profile)) return;

                                try {
                                    lines.add(" &r&c" + p.getName() + " &7(" + profile.getPlayer().getPing() + ")");
                                } catch (Exception ignored) {}
                            });
                        }
                        
                        lines.add("&r&9");
                        lines.add("&l&6|&r &fYour Ping:&6 " + profile.getPlayer().getPing() + "ms");
                    } catch (Exception ignored) {}
                }
            }
        } else {
            switch (state) {
                case STARTING, RUNNING -> {
                    Profile opponent = getOpponentProfile(profile);

                    if (state.equals(MatchState.STARTING)) {
                        lines.add("&l&6|&r&f Opponent:&6 " + opponent.getName());
                        lines.add("&r&4");
                    }

                    try {
                        lines.add("&l&6|&r &fYour Ping:&6 " + profile.getPlayer().getPing() + "ms");
                        lines.add("&l&6|&r &fTheir Ping:&6 " + opponent.getPlayer().getPing() + "ms");
                    } catch (Exception ignored) {}
                }
            }
        }

        return lines;
    }

    private void getKit(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();

            profile.clear();
            player.setGamemode(Player.SURVIVAL);
            player.setMaxHealth(player.getMaxHealth());

            eventArena.getEvent().getKit().giveKit(profile);
        } catch (Exception ignored) {}
    }

    protected void broadcast(String message) {
        if (eventArena.getEvent().isTeam()) {
            getAllPlayers().forEach(profile -> {
                try {
                    Player player = profile.getPlayer();
                    player.sendMessage(TextFormat.colorize(message));
                } catch (Exception ignore) {}
            });
            return;
        }

        if (firstProfile.isOnline()) {
            try {
                firstProfile.getPlayer().sendMessage(TextFormat.colorize(message));
            } catch (Exception ignored) {}
        }

        if (secondProfile.isOnline()) {
            try {
                secondProfile.getPlayer().sendMessage(TextFormat.colorize(message));
            } catch (Exception ignored) {}
        }
    }

    public void checkWinner() {
        if (eventArena.getEvent().isTeam()) {
            if (getTeamAlives(firstTeam) == 0 || getTeamAlives(secondTeam) == 0) {
                stop((getTeamAlives(secondTeam) == 0 ? firstTeam : secondTeam), false);
            }
            return;
        }

        if (isSpectatorInMatch(firstProfile) || isSpectatorInMatch(secondProfile)) {
            stop((isSpectatorInMatch(secondProfile) ? firstProfile : secondProfile), false);
        }
    }

    public void stop() {
        if (eventArena.getEvent().isTeam()) stop((Team) null, true);
        else stop((Profile) null, true);
    }

    public void stop(@Nullable Profile profile, boolean forceStop) {
        state = MatchState.ENDING;

        if (firstProfile.isOnline()) {
            try {
                Player player = firstProfile.getPlayer();
                firstProfile.getCacheData().clearCooldown();
                firstProfile.clear();
                player.setImmobile(false);
            } catch (Exception ignored) {}
        }

        if (secondProfile.isOnline()) {
            try {
                Player player = secondProfile.getPlayer();
                secondProfile.clear();
                secondProfile.getCacheData().clearCooldown();
                player.setImmobile(false);
            } catch (Exception ignored) {}
        }

        if (profile != null) {
            if (profile.isOnline()) {
                try {
                    Player winnerPlayer = profile.getPlayer();
                    Fireworks.spawnFirework(winnerPlayer);

                    winnerPlayer.teleport(winnerPlayer.getServer().getDefaultLevel().getSpawnLocation());

                    winnerPlayer.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                    winnerPlayer.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());

                    winnerPlayer.sendTitle(TextFormat.colorize("&l&aVICTORY&r"), TextFormat.colorize("&7You won the fight!"));
                } catch (Exception ignored) {}
            }

            Profile opponentProfile = getOpponentProfile(profile);

            if (opponentProfile.isOnline()) {
                try {
                    eventArena.getEvent().removePlayer(opponentProfile);

                    eventArena.getEvent().broadcast("&c" + opponentProfile.getName() + " Has been eliminated from the tournament");
                    opponentProfile.getPlayer().sendTitle(TextFormat.colorize("&l&cDEFEAT&r"), TextFormat.colorize("&a" + profile.getName() + " &7won the fight!"));
                } catch (Exception ignored) {}
            }

            String spectators = getSpectators().stream()
                    .filter(s -> s.getProfileData().isSpectator())
                    .map(Profile::getName)
                    .collect(Collectors.joining(", "));

            if (!spectators.isEmpty()) {
                broadcast("&aSpectators: " + spectators);
            }
        }

        if (forceStop) {
            if (firstProfile.isOnline()) {
                try {
                    Player player = firstProfile.getPlayer();
                    player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());

                    player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                    player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());
                } catch (Exception ignored) {}
            }

            if (secondProfile.isOnline()) {
                try {
                    Player player = secondProfile.getPlayer();
                    player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());

                    player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                    player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());
                } catch (Exception ignored) {}
            }
        }

        getSpectators().stream().filter(s -> s.getProfileData().isSpectator()).forEach(eventArena::setRandomSpectate);
        spectators.clear();
        delete();
    }

    public boolean isSpectator(Profile profile) {
        return spectators.contains(profile.getIdentifier());
    }

    public void addSpectator(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();

            if (player.getGamemode() != Player.SPECTATOR) {
                profile.clear();
                profile.getCacheData().getCombat().clear();
                profile.getCacheData().clearCooldown();

                player.setGamemode(Player.SPECTATOR);
            }

            player.teleport(world.getBlock(worldData.getFirstPosition()).getLocation());
            spectators.add(profile.getIdentifier());

            player.getLevel().getPlayers().values().forEach(player::showPlayer);
        } catch (Exception ignored) {}
    }

    public void removeSpectator(Profile profile) {
        spectators.remove(profile.getIdentifier());
    }

    public void stop(@Nullable Team team, boolean forceStop) {
        state = MatchState.ENDING;

        getAllPlayers().forEach(profile -> {
            try {
                profile.clear();
                profile.getCacheData().clearCooldown();

                Player player = profile.getPlayer();
                player.setImmobile(false);
            } catch (Exception ignore) {}
        });

        if (team != null) {
            team.getMembers().forEach(profile -> {
                try {
                    Player player = profile.getPlayer();
                    Fireworks.spawnFirework(player);

                    player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());

                    player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                    player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());

                    player.sendTitle(TextFormat.colorize("&l&aVICTORY&r"), TextFormat.colorize("&7You team won the fight!"));
                } catch (Exception ignored) {}
            });

            Team opponentTeam = team.getId() == firstTeam.getId() ? secondTeam : firstTeam;

            eventArena.getEvent().broadcast("&cTeam #" + opponentTeam.getId() + " Has been eliminated from the tournament");
            opponentTeam.getMembers().forEach(profile -> {
                try {
                    profile.getPlayer().sendTitle(TextFormat.colorize("&l&cDEFEAT&r"), TextFormat.colorize("&aThe opposing team &7won the fight!"));

                    eventArena.getEvent().removePlayer(profile);
                } catch (Exception ignored) {}
            });

            String spectator = getSpectators().stream()
                    .filter(s -> s.getProfileData().isSpectator())
                    .map(Profile::getName)
                    .collect(Collectors.joining(", "));

            if (!spectator.isEmpty()) {
                broadcast("&aSpectators: " + spectator);
            }
        }

        if (forceStop) {
            getAllPlayers().forEach(profile -> {
                try {
                    Player player = profile.getPlayer();

                    player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());

                    player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                    player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());
                } catch (Exception ignore) {}
            });
        }

        getSpectators().stream().filter(s -> s.getProfileData().isSpectator()).forEach(eventArena::setRandomSpectate);
        spectators.clear();
        delete();
    }

    public boolean isLimitY(int y) {
        return (this.worldData.getFirstPosition().getFloorY() + 6) <= y;
    }

    public void removeBlock(Block block) {
        blockData.remove(block.getLocation().toString());
    }

    public boolean isBlock(Block block) {
        Location blockPosition = block.getLocation();
        return blockPosition.getLevel().getFolderName().equals(getWorld().getFolderName()) &&
                blockData.contains(blockPosition.toString());
    }

    public void addBlock(Block block) {
        blockData.add(block.getLocation().toString());
    }

    private List<Profile> getMembersOnline(List<Profile> profiles) {
        return profiles.stream().filter(this::isValid).toList();
    }

    private boolean isFirstTeam(Profile profile) {
        return firstTeam.isMember(profile);
    }

    private Team getOpponentTeam(Profile profile) {
        if (isFirstTeam(profile)) return secondTeam;

        return firstTeam;
    }

    private boolean isValid(Profile profile) {
        return profile.isOnline() && profile.getProfileData().getEvent() != null && profile.getProfileData().getEvent().getEventArena() != null && profile.getProfileData().getEvent().getEventArena().equals(eventArena);
    }

    public List<Profile> getAllPlayers() {
        List<Profile> allMembers = new ArrayList<>();
        allMembers.addAll(firstTeam.getMembers());
        allMembers.addAll(secondTeam.getMembers());

        return allMembers;
    }

    public Profile getOpponentProfile(Profile profile) {
        if (profile.getIdentifier().equals(firstProfile.getIdentifier())) {
            return secondProfile;
        }
        return firstProfile;
    }

    protected boolean isFirstProfile(Profile profile) {
        return firstProfile.getIdentifier().equals(profile.getIdentifier());
    }

    public boolean inThisMatch(Profile profile) {
        if (eventArena.getEvent().isTeam()) {
            return firstTeam.isMember(profile) || secondTeam.isMember(profile);
        }

        return firstProfile.getIdentifier().equals(profile.getIdentifier()) || secondProfile.getIdentifier().equals(profile.getIdentifier());
    }

    public boolean isAlive(Profile profile) {
        return inThisMatch(profile) && !isSpectator(profile);
    }

    public boolean isSpectatorInMatch(Profile profile) {
        return inThisMatch(profile) && isSpectator(profile);
    }

    public int getTeamAlives(Team team) {
        return (int) team.getMembersAlive().stream().filter(profile -> !isSpectatorInMatch(profile)).count();
    }

    private List<Profile> getSpectators() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.spectators.contains(profile.getIdentifier()) &&
                        profile.isOnline())
                .collect(Collectors.toList());
    }

    protected void delete() {
        eventArena.removeDuel(id);

        String worldName = world.getName();
        String path = Practice.getInstance().getServer().getDataPath() + File.separator + "worlds";

        Practice.getInstance().getServer().unloadLevel(world);
        Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new DeleteWorldAsync(worldName, path));
    }

}
