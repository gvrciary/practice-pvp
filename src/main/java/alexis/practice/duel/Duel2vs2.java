package alexis.practice.duel;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Fireworks;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Duel2vs2 extends Duel {

    protected final int id;

    protected List<String> firstTeam;
    protected List<String> secondTeam;

    protected List<String> winnerTeam;
    protected List<String> loserTeam;

    protected Level world;
    protected DuelWorld worldData;

    protected Kit kit;

    public Duel2vs2(int id, List<String> firstTeam, List<String> secondTeam, Level world, DuelWorld duelWorld, Kit kit) {
        this.id = id;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;

        this.world = world;
        this.worldData = duelWorld;
        this.kit = kit;

        getTeam(firstTeam).forEach(profile -> profile.getProfileData().setDuel(this));
        getTeam(secondTeam).forEach(profile -> profile.getProfileData().setDuel(this));

        setup();
    }

    @Override
    public void setup() {
        if (getTeam(firstTeam).isEmpty()|| getTeam(secondTeam).isEmpty()) {
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
                profile.getCacheData().getCombat().clear();
                getKit(profile);
            } catch (Exception ignore) {}
        });
    }

    @Override
    public void setDeath(Profile profile) {
        Profile lastHit = profile.getCacheData().getCombat().get();
        if (lastHit != null) profile.setDeathAnimation(lastHit);
        addSpectator(profile);

        if (getTeamAlive((isFirstTeam(profile) ? getTeam(firstTeam) : getTeam(secondTeam))) == 0) {
            stop(getOpponentTeam(profile));
        }
    }

    @Override
    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();
        switch (state) {
            case STARTING, RUNNING -> {
                if (isSpectator(profile) && profile.getProfileData().isSpectator()) {
                    try {
                        getTeam(firstTeam).forEach(p -> {
                            if (!isValid(profile)) return;

                            try {
                                lines.add(" &r&f" + p.getName() + " &7(" + profile.getPlayer().getPing() + ")");
                            } catch (Exception ignored) {}
                        });
                        lines.add(" &r&6VS");
                        getTeam(secondTeam).forEach(p -> {
                            if (!isValid(profile)) return;

                            try {
                                lines.add(" &r&f" + p.getName() + " &7(" + profile.getPlayer().getPing() + ")");
                            } catch (Exception ignored) {}
                        });
                    } catch (Exception ignored) {}

                    lines.add("&l&6|&r&f Duration:&6 " + Utils.formatTime(runTime));
                    lines.add("&l&6|&r&f Mode:&6 2vs2" + " : " + kit.getCustomName());
                    return lines;
                }

                List<Profile> opponents = getOpponentTeam(profile);
                List<Profile> yourTeam = isFirstTeam(profile) ? getTeam(firstTeam) : getTeam(secondTeam);

                if (state.equals(DuelState.STARTING)) {
                    lines.add("&l&6|&r&f Opponents: ");
                    opponents.forEach(p -> {
                        if (!isValid(profile)) return;

                        try {
                            lines.add(" &r&f" + p.getName() + " &7(" + profile.getPlayer().getPing() + ")");
                        } catch (Exception ignored) {}
                    });
                    lines.add("&r&4");
                }

                try {
                    if (yourTeam.size() > 1) {
                        lines.add("&l&6|&r &fYour Team: ");
                        yourTeam.stream().filter(p -> !profile.getIdentifier().equals(p.getIdentifier())).forEach(p -> {
                            if (!isValid(profile)) return;

                            try {
                                lines.add(" &r&c" + p.getName() + " &7(" + profile.getPlayer().getPing() + ")");
                            } catch (Exception ignored) {}
                        });
                    }

                    if (state.equals(DuelState.RUNNING)) {
                        lines.add("&l&6|&r &fTheir Team: ");
                        opponents.forEach(p -> {
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

            case ENDING -> {
                if (winnerTeam != null && winnerTeam.contains(profile.getIdentifier())) {
                    lines.add("&l&6|&a VICTORY");
                } else if (loserTeam != null && loserTeam.contains(profile.getIdentifier())) {
                    lines.add("&l&6|&c DEFEAT");
                }

                lines.add("&r&9");
                lines.add("&l&6|&r&f Duration:&6 " + Utils.formatTime(runTime));
            }
        }

        return lines;
    }

    @Override
    public void tick() {
        switch (state) {
            case STARTING -> {
                if (startTime <= 0) {
                    start();
                    return;
                }

                broadcast("&6" + startTime + "...");
                getAllPlayers().forEach(profile -> {
                    try {
                        PlayerUtil.playSound(profile.getPlayer(), "random.click");
                    } catch (Exception ignored) {}
                });

                startTime--;
            }
            case RUNNING -> runTime++;
            case ENDING -> {
                if (--endTime <= 0) {
                    getAllPlayers().forEach(profile -> {
                        profile.clear();
                        profile.getProfileData().setDuel();

                        try {
                            PlayerUtil.getLobbyKit(profile.getPlayer());
                        } catch (Exception ignored) {}
                    });

                    getSpectators().stream().filter(profile -> profile.getProfileData().isSpectator()).forEach(spectator -> spectator.getProfileData().setSpectate());

                    delete();
                }
            }
        }
    }

    @Override
    public void stop() {
        stop(Collections.emptyList());
    }

    public void stop(List<Profile> team) {
        state = DuelState.ENDING;

        if (!team.isEmpty()) {
            if (team.equals(getTeam(firstTeam))) {
                winnerTeam = firstTeam;
                loserTeam = secondTeam;
            } else {
                winnerTeam = secondTeam;
                loserTeam = firstTeam;
            }

            team.forEach(profile -> {
                try {
                    Player player = profile.getPlayer();
                    Fireworks.spawnFirework(player);

                    player.sendTitle(TextFormat.colorize("&l&aVICTORY&r"), TextFormat.colorize("&7You team won the fight!"));
                } catch (Exception ignored) {}
            });

            getTeam(loserTeam).forEach(profile -> {
                try {
                    profile.getPlayer().sendTitle(TextFormat.colorize("&l&cDEFEAT&r"), TextFormat.colorize("&aThe opposing team &7won the fight!"));
                } catch (Exception ignored) {}
            });

            broadcast("&l&6Match Results\n&r&aWinner:&f " + getTeamNames(getTeam(winnerTeam)) + "&8 |&c Loser:&f " + getTeamNames(getTeam(loserTeam)));

            String spectators = getSpectators().stream()
                    .filter(s -> s.getProfileData().isSpectator())
                    .map(Profile::getName)
                    .collect(Collectors.joining(", "));

            if (!spectators.isEmpty()) {
                broadcast("&aSpectators: " + spectators);
            }
        }

        getAllPlayers().forEach(profile -> {
            try {
                profile.clear();
                profile.getCacheData().clearCooldown();
                profile.getCacheData().getCombat().clear();

                Player player = profile.getPlayer();
                player.setImmobile(false);
            } catch (Exception ignore) {}
        });
    }

    @Override
    public void start() {
        if (getTeam(firstTeam).isEmpty()|| getTeam(secondTeam).isEmpty()) {
            stop();
            return;
        }

        state = DuelState.RUNNING;

        getAllPlayers().forEach(profile -> {
            try {
                Player player = profile.getPlayer();
                player.getLevel().getPlayers().values().forEach(player::showPlayer);
                player.setImmobile(false);
            } catch (Exception ignore) {}
        });

        broadcast("&aMatch started");
    }

    public void remove(Profile profile) {
        if (!isSpectator(profile)) {
            setDeath(profile);
        }

        if (isFirstTeam(profile)) firstTeam.remove(profile.getIdentifier());
        else secondTeam.remove(profile.getIdentifier());
    }

    @Override
    protected void broadcast(String message) {
        getAllPlayers().forEach(profile -> {
            try {
                Player player = profile.getPlayer();
                player.sendMessage(TextFormat.colorize(message));
            } catch (Exception ignore) {}
        });
    }

    private List<Profile> getTeam(List<String> team) {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> team.contains(profile.getIdentifier()) &&
                        profile.isOnline())
                .collect(Collectors.toList());
    }

    private boolean isFirstTeam(Profile profile) {
        return firstTeam.contains(profile.getIdentifier());
    }

    private List<Profile> getOpponentTeam(Profile profile) {
        if (isFirstTeam(profile)) return getTeam(secondTeam);

        return getTeam(firstTeam);
    }

    private boolean isValid(Profile profile) {
        return profile.isOnline() && profile.getProfileData().getDuel() != null && profile.getProfileData().getDuel().equals(this);
    }

    private int getTeamAlive(List<Profile> profiles) {
        return (int) profiles.stream().filter(profile -> !isSpectator(profile)).count();
    }

    private String getTeamNames(List<Profile> profiles) {
        return profiles.stream().map(Profile::getName)
                .collect(Collectors.joining(", "));
    }

    public List<Profile> getAllPlayers() {
        List<Profile> allMembers = new ArrayList<>();
        allMembers.addAll(getTeam(firstTeam));
        allMembers.addAll(getTeam(secondTeam));

        return allMembers;
    }

}
