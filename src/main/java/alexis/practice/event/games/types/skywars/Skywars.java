package alexis.practice.event.games.types.skywars;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.games.EventState;
import alexis.practice.event.games.types.skywars.chest.ChestManager;
import alexis.practice.event.team.Team;
import alexis.practice.event.world.EventWord;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Skywars extends EventArena {

    @Getter
    private final ChestManager chestManager;

    private int refill = 3 * 60;

    public Skywars(int id, Event event, EventWord worldData, Level world) {
        super(id, event, worldData, world);

        this.chestManager = new ChestManager();
        this.canDropItem = true;
    }

    public void start() {
        super.start();

        AtomicInteger size = new AtomicInteger();

        if (event.isTeam()) {
            getPlayers().forEach(profile -> {
                Team team = event.getTeamManager().getTeam(profile);

                if (team == null) {
                    event.getTeamManager().setInRandomTeam(profile);
                }
            });

            event.getTeamManager().getTeamsAlive().forEach(team -> {
                Vector3 spawn = worldData.getSpawns().get(size.getAndIncrement());

                team.getMembersAlive().forEach(profile -> {
                    try {
                        Player player = profile.getPlayer();
                        player.teleport(world.getBlock(spawn));
                        player.setImmobile();
                    } catch (Exception ignored) {}
                });
            });

            return;
        }

        getPlayers().forEach(profile -> {
            Vector3 spawn = worldData.getSpawns().get(size.getAndIncrement());

            try {
                Player player = profile.getPlayer();
                player.teleport(world.getBlock(spawn));
                player.setImmobile();
            } catch (Exception ignored) {}
        });

        currentState = EventState.STARTING;
    }

    public void tick() {
        switch (currentState) {
            case STARTING -> {
                super.tick();
                if (startTime == 0) {
                    getPlayers().forEach(profile -> {
                        try {
                            Player player = profile.getPlayer();

                            player.setImmobile(false);
                            player.getLevel().getPlayers().values().forEach(player::showPlayer);
                            Block block = player.getLevel().getBlock(player.getPosition().subtract(0, 1));

                            if (block.getId() == BlockID.GLASS) {
                                player.getLevel().setBlock(block.getLocation(), Block.get(Block.AIR));
                            }
                        } catch (Exception ignored) {}
                    });

                    currentState = EventState.RUNNING;
                }
            }

            case RUNNING -> {
                super.tick();

                if (refill-- == 0) {
                    chestManager.refillChest();
                    event.broadcast("&6All chests have been refilled");
                }
            }
            default -> super.tick();
        }
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        switch (currentState) {
            case WAITING -> {
                List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(Profile::isOnline).toList();
                lines.add("&l&6|&r &fOnline:&6 " + players.size());
                lines.add("&r&9");
                lines.add(" &l&6SkyWars");
                lines.add("&l&6|&r &fMode:&6 " + (event.isTeam() ? "TO" + event.getCountTeam() : "FFA"));
                lines.add("&l&6|&r &fRemaining:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                lines.add("&l&6|&r &fMap:&6 " + world.getName());
                lines.add("&r&f");
                if (getPlayers().size() > 1) lines.add("&l&6|&r &fStart in:&6 " + Utils.formatTime(waitingTime));
            }
            case STARTING -> {
                lines.add("&l&6|&r &fMap:&6 " + world.getName());
                lines.add("&r&f");
                lines.add("&l&6|&r &fMatch Start in:&6 " + startTime + "s");
            }
            case RUNNING -> {
                if (isSpectator(profile)) {
                    lines.add("&l&6|&r &fMode:&6 " + (event.isTeam() ? "TO" + event.getCountTeam() : "FFA"));
                    lines.add("&l&6|&r &fMap:&6 " + world.getName());
                    lines.add("&l&6|&r &fPlayers:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                    lines.add("&r&f");
                    lines.add("&l&6|&r &fDuration: &6" + Utils.formatTime(runTime));
                    if (refill > 0) lines.add("&l&6|&r &fRefill in: &6" + Utils.formatTime(refill));
                    break;
                }

                lines.add("&l&6|&r &fDuration: &6" + Utils.formatTime(runTime));
                lines.add("&l&6|&r &fRemaining:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                lines.add("&r&f");
                lines.add("&l&6|&r &fKills: &6" + getKills(profile));
                if (refill > 0) lines.add("&l&6|&r &fRefill in: &6" + Utils.formatTime(refill));
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
}
