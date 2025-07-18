package alexis.practice.event.games.types.sumo;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.games.EventState;
import alexis.practice.event.team.Team;
import alexis.practice.event.world.EventWord;
import alexis.practice.item.HotbarItem;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Sumo extends EventArena {
    @Nullable
    protected Profile firstProfile;
    @Nullable
    protected Profile secondProfile;

    @Nullable
    protected Team firstTeam;
    @Nullable
    protected Team secondTeam;

    protected int countdownCombat = 5;

    protected int rounds = 0;

    public Sumo(int id, Event event, EventWord worldData, Level world) {
        super(id, event, worldData, world);
    }

    public void start() {
        super.start();
        if (event.isTeam()) {
            getPlayers().forEach(profile -> {
                Team team  = event.getTeamManager().getTeam(profile);

                if (team == null) {
                    event.getTeamManager().setInRandomTeam(profile);
                }
            });

            if ((!event.isTeam() && getPlayers().size() <= 1) || (event.isTeam() && event.getTeamManager().getTeamsAlive().size() <= 1)) {
                stop();
                return;
            }

            event.broadcast("&6Sumo started");
            event.broadcast("&aGood Luck");

            assignFight();
            currentState = EventState.RUNNING;
        }

        getPlayers().forEach(profile -> {
           profile.clear();

            try {
                Player player = profile.getPlayer();

                player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());

                player.getLevel().getPlayers().values().forEach(player::showPlayer);
            } catch (Exception ignored) {}
        });
    }

    public void tick() {
        switch (currentState) {
            case WAITING, ENDING -> super.tick();
            case RUNNING -> {
                if (countdownCombat == 0) {
                    startFight();
                    return;
                }

                if (countdownCombat > -1) {
                    countdownCombat--;
                }

                super.tick();
            }
        }
    }

    public void assignFight() {
        if (event.isTeam()) {
            if (firstTeam != null && firstTeam.isAlive() && secondTeam != null && secondTeam.isAlive()) {
                return;
            }

            if (firstTeam != null) {
                firstTeam.getMembers().forEach(profile -> {
                    profile.clear();
                    profile.getCacheData().clearCooldown();
                    profile.getCacheData().getCombat().clear();

                    try {
                        Player player = profile.getPlayer();

                        player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                        player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());
                        player.teleport(world.getBlock(worldData.getLobbyPosition()));
                    } catch (Exception ignored) {}
                });

                if (firstTeam.isAlive()) {
                    event.broadcast("&aTeam " + firstTeam.getId() + " has won the fight");
                    rounds++;
                }
            }

            if (secondTeam != null) {
                secondTeam.getMembers().forEach(profile -> {
                    profile.clear();
                    profile.getCacheData().clearCooldown();
                    profile.getCacheData().getCombat().clear();

                    try {
                        Player player = profile.getPlayer();

                        player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                        player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());
                        player.teleport(world.getBlock(worldData.getLobbyPosition()));
                    } catch (Exception ignored) {}
                });

                if (secondTeam.isAlive()) {
                    event.broadcast("&aTeam " + secondTeam.getId() + " has won the fight");
                    rounds++;
                }
            }

            if (event.getTeamManager().getTeamsAlive().size() <= 1) {
                stop();
                return;
            }

            firstTeam = null;
            secondTeam = null;
            countdownCombat = 5;

            List<Team> teams = new ArrayList<>(event.getTeamManager().getTeamsAlive());
            Collections.shuffle(teams);

            firstTeam = teams.get(0);
            secondTeam = teams.get(1);

            if (firstTeam != null && secondTeam != null) {
                firstTeam.getMembers().forEach(profile -> {
                    try {
                        Player player = profile.getPlayer();

                        player.setImmobile();
                        player.teleport(world.getBlock(worldData.getFirstPosition()));
                        getKit(profile);
                    } catch (Exception ignored) {}
                });

                secondTeam.getMembers().forEach(profile -> {
                    try {
                        Player player = profile.getPlayer();

                        player.setImmobile();
                        player.teleport(world.getBlock(worldData.getSecondPosition()));
                        getKit(profile);
                    } catch (Exception ignored) {}
                });
            }

            return;
        }

        if (firstProfile != null && firstProfile.isOnline()) {
            if (!isSpectator(firstProfile)) {
                event.broadcast("&6" + firstProfile.getName() + " has won the fight");
                rounds++;
            }

            firstProfile.clear();

            try {
                Player player = firstProfile.getPlayer();

                player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());
                player.teleport(world.getBlock(worldData.getLobbyPosition()));
            } catch (Exception ignored) {}
        }

        if (secondProfile != null && secondProfile.isOnline()) {
            if (!isSpectator(secondProfile)) {
                event.broadcast("&6" + secondProfile.getName() + " has won the fight");
                rounds++;
            }
            secondProfile.clear();

            try {
                Player player = secondProfile.getPlayer();

                player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
                player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());
                player.teleport(world.getBlock(worldData.getLobbyPosition()));
            } catch (Exception ignored) {}
        }

        if (getPlayers().size() <= 1) {
            stop();
            return;
        }

        firstProfile = null;
        secondProfile = null;
        countdownCombat = 5;

        List<Profile> players = new ArrayList<>(getPlayers());
        Collections.shuffle(players);

        firstProfile = players.get(0);
        secondProfile = players.get(1);

        if (firstProfile != null && secondProfile != null) {
            getKit(secondProfile);
            getKit(firstProfile);

            try {
                if (firstProfile.isOnline()) {
                    Player firstPlayer = firstProfile.getPlayer();
                    firstPlayer.setImmobile();
                    firstPlayer.teleport(world.getBlock(worldData.getFirstPosition()));
                }

                if (secondProfile.isOnline()) {
                    Player secondPlayer = secondProfile.getPlayer();
                    secondPlayer.teleport(world.getBlock(worldData.getSecondPosition()));
                    secondPlayer.setImmobile();
                }
            } catch (Exception ignored) {}
        }
    }

    public void getKit(Profile profile) {
        if (!profile.isOnline()) return;

        profile.clear();
        profile.getCacheData().clearCooldown();
        profile.getCacheData().getCombat().clear();

        try {
            event.getKit().giveKit(profile);
        } catch (Exception ignored) {}
    }

    public void startFight() {
        if (event.isTeam()) {
            if (event.getTeamManager().getTeamsAlive().size() <= 1 || !Objects.requireNonNull(firstTeam).isAlive() || !Objects.requireNonNull(secondTeam).isAlive()) {
                stop();
                return;
            }

            firstTeam.getMembers().forEach(profile -> {
                try {
                    Player player = profile.getPlayer();

                    player.setGamemode(Player.SURVIVAL);
                    player.setImmobile(false);
                    PlayerUtil.playSound(player, "random.click");
                } catch (Exception ignored) {}
            });

            secondTeam.getMembers().forEach(profile -> {
                try {
                    Player player = profile.getPlayer();

                    player.setGamemode(Player.SURVIVAL);
                    player.setImmobile(false);
                    PlayerUtil.playSound(player, "random.click");
                } catch (Exception ignored) {}
            });

            return;
        }

        if (getPlayers().size() <= 1 || !Objects.requireNonNull(firstProfile).isOnline() || !Objects.requireNonNull(secondProfile).isOnline()) {
            stop();
            return;
        }

        try {
            Player player = firstProfile.getPlayer();

            player.setGamemode(Player.SURVIVAL);
            player.setImmobile(false);
            PlayerUtil.playSound(player, "random.click");
        } catch (Exception ignored) {}

        try {
            Player player = secondProfile.getPlayer();

            player.setGamemode(Player.SURVIVAL);
            player.setImmobile(false);
            PlayerUtil.playSound(player, "random.click");
        } catch (Exception ignored) {}
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        switch (currentState) {
            case STARTING -> {
                List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(Profile::isOnline).toList();
                lines.add("&l&6|&r &fOnline:&6 " + players.size());
                lines.add("&r&9");
                lines.add(" &l&6Sumo");
                lines.add("&l&6|&r &fMode:&6 " + (event.isTeam() ? "TO" + event.getCountTeam() : "FFA"));
                lines.add("&l&6|&r &fLadder: &6" + event.getKit().getName());
                lines.add("&l&6|&r &fPlayers:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                if (getPlayers().size() > 1) {
                    lines.add("&r&7");
                    lines.add("&l&6|&r &fStart In:&6 " + Utils.formatTime(waitingTime));
                }
            }
            case RUNNING -> {
                if (firstProfile != null && secondProfile != null) {
                    try {
                        lines.add("&l&6|&r &fFight: ");
                        lines.add(" &f- " + firstProfile.getName() + "&7 (" + firstProfile.getPlayer().getPing() + "ms)");
                        lines.add(" &f- " + secondProfile.getName() + "&7 (" + secondProfile.getPlayer().getPing() + "ms)");
                    } catch (Exception ignored) {}
                } else if (firstTeam != null && secondTeam != null) {
                    lines.add("&l&6|&r &fFight: ");
                    lines.add(" &f- Team #" + firstTeam.getId());
                    lines.add(" &f- Team #" + secondTeam.getId());
                }

                lines.add("&r ");
                if (countdownCombat > -1) {
                    lines.add("&l&6|&r &fFight in: &6" + countdownCombat);
                }

                lines.add("&l&6|&r &fPlayers:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
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

    public boolean inFight(Profile profile) {
        if (event.isTeam()) {
            Team team = event.getTeamManager().getTeam(profile);

            if (team == null) return false;

            return ((firstTeam != null && firstTeam.getId() == team.getId()) || (secondTeam != null && secondTeam.getId() == team.getId()));
        }

        return ((firstProfile != null && firstProfile.getIdentifier().equals(profile.getIdentifier())) || (secondProfile != null && secondProfile.getIdentifier().equals(profile.getIdentifier())));
    }

}
