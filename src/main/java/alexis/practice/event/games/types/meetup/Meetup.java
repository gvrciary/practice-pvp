package alexis.practice.event.games.types.meetup;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.games.EventState;
import alexis.practice.event.games.types.meetup.border.Border;
import alexis.practice.event.games.types.meetup.kit.KitStorage;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import alexis.practice.event.games.types.meetup.scenarios.ScenarioManager;
import alexis.practice.event.games.types.meetup.scenarios.defaults.DoNotDisturb;
import alexis.practice.event.games.types.meetup.scenarios.defaults.NoClean;
import alexis.practice.event.team.Team;
import alexis.practice.event.world.EventWord;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Meetup extends EventArena {
    private int timeToShrink = 90;

    private final Border border;
    private final ScenarioManager scenarioManager;
    private final List<KitStorage> kits = Arrays.stream(KitStorage.values()).toList();

    public Meetup(int id, Event event, EventWord worldData, Level world) {
        super(id, event, worldData, world);
        world.setSpawnLocation(new Vector3(0, world.getHighestBlockAt(0, 0) + 1, 0));

        this.startTime = 20;
        this.canDropItem = true;
        this.border = new Border(this, world);
        this.border.shrink();
        this.scenarioManager = new ScenarioManager(this);
    }

    public void scattering() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (event.isTeam()) {
                event.getTeamManager().getTeamsAlive().forEach(team -> {
                    Position position = getRandomPosition();

                    team.getMembersAlive().forEach(member -> {
                        setKit(member);

                        try {
                            member.getPlayer().teleport(position);
                        } catch (Exception ignored) {}
                    });
                });
                return;
            }

            getPlayers().stream()
                    .filter(p -> p.isOnline() && isAlive(p))
                    .forEach(profile -> {
                        Position position = getRandomPosition();

                        setKit(profile);
                        try {
                            profile.getPlayer().teleport(position);
                        } catch (Exception ignored) {}
                    });
        });

        executor.shutdown();
    }

    public void start() {
        super.start();
        if (event.isTeam()) {
            getPlayers().forEach(profile -> {
                Team team = event.getTeamManager().getTeam(profile);

                if (team == null) {
                    event.getTeamManager().setInRandomTeam(profile);
                }
            });
        }

        scattering();
        currentState = EventState.STARTING;
    }

    public void setKit(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();

            profile.clear();
            player.setImmobile();
            player.setGamemode(Player.SURVIVAL);
            KitStorage kit = kits.get(Utils.randomInteger(0, kits.size() - 1));
            player.getInventory().setContents(kit.getInventory());
            player.getInventory().setArmorContents(kit.getArmor());

            player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", (player.getHealth() + player.getAbsorption()))));
        } catch (Exception ignored) {}
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        switch (currentState) {
            case WAITING-> {
                final List<Scenario> enabledScenarios = scenarioManager.getScenarios().values().stream()
                        .filter(Scenario::isEnabled)
                        .toList();

                List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(Profile::isOnline).toList();
                lines.add("&l&6|&r &fOnline:&6 " + players.size());
                lines.add("&r&9");
                lines.add(" &l&6Meetup");
                lines.add("&l&6|&r &fMode:&6 " + (event.isTeam() ? "TO" + event.getCountTeam() : "FFA"));
                lines.add("&l&6|&r &fPlayers:&6 " + getPlayers().size() + "/" + event.getMaxPlayers() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                lines.add("&r&7");
                lines.add("&l&6|&r &fScenarios: ");

                if (enabledScenarios.isEmpty()) lines.add("&c  No Scenarios");
                else enabledScenarios.forEach(scenario -> lines.add("&f  -&6 " + scenario.getName()));

                if (getPlayers().size() > 1) {
                    lines.add("&r&8");
                    lines.add("&l&6|&r &fStart In: &6" + Utils.formatTime(waitingTime));
                }
            }
            case STARTING -> lines.add("&l&6|&r &fUHC Meetup Start in:&6 " + startTime);

            case RUNNING -> {
                if (isSpectator(profile)) {
                    lines.add("&l&6|&r &fMode:&6 " + (event.isTeam() ? "TO" + event.getCountTeam() : "FFA"));
                    lines.add("&l&6|&r &fBorder: &6" + border.getSize() + (border.getSize() > 25 ? " &7(" + timeToShrink + ")" : ""));
                    lines.add("&l&6|&r &fRemaining: &6" + getPlayers().size() + "/" + event.getPlayers().size());
                    lines.add("&l&6|&r &fGame Time: &6" + Utils.formatTime(runTime));
                    break;
                }

                lines.add("&l&6|&r &fRemaining: &6" + getPlayers().size() + "/" + event.getPlayers().size() + (event.isTeam() ? " &7(" + event.getTeamManager().getTeamsAlive().size() + ")" : ""));
                lines.add("&l&6|&r &fGame Time: &6" + Utils.formatTime(runTime));
                lines.add("&l&6|&r &fKills: &6" + getKills(profile) + (event.isTeam() ? " &7(" + Objects.requireNonNull(event.getTeamManager().getTeam(profile)).getKills() + ")" : ""));
                lines.add("&l&6|&r &fBorder: &6" + border.getSize() + (border.getSize() > 25 ? " &7(" + timeToShrink + ")" : ""));

                Scenario noclean = scenarioManager.get(ScenarioManager.NOCLEAN);
                if (noclean.isEnabled() && noclean instanceof NoClean && ((NoClean) noclean).isValid(profile)) {
                    lines.add("&l&6|&r &fNo Clean: &6" + Utils.formatTime(((NoClean) noclean).get(profile) - System.currentTimeMillis()));
                }

                Scenario dnd = scenarioManager.get(ScenarioManager.DONOTDISTURB);
                if (dnd.isEnabled() && dnd instanceof DoNotDisturb && ((DoNotDisturb) dnd).inData(profile)) {
                    lines.add("&l&6|&r &fDND: &6" + Utils.formatTime(((DoNotDisturb) dnd).getTime(profile) - System.currentTimeMillis()));
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

    public void tick() {
        switch (currentState) {
            case WAITING, ENDING -> super.tick();
            case STARTING -> {
                if (startTime <= 0) {
                    if ((!event.isTeam() && getPlayers().size() <= 1) || (event.isTeam() && event.getTeamManager().getTeamsAlive().size() <= 1)) stop();

                    event.broadcast("&6UHC Meetup started");
                    event.broadcast("&aPVP has been activated");
                    currentState = EventState.RUNNING;
                    return;
                }

                if (startTime == 15) {
                    getPlayers().forEach(profile -> {
                        try {
                            Player player = profile.getPlayer();
                            player.getLevel().getPlayers().values().forEach(player::showPlayer);
                            player.setImmobile(false);
                        } catch (Exception ignored) {}
                    });

                    event.broadcast("&aYou can move now");
                }

                super.tick();
            }

            case RUNNING -> {
                if (timeToShrink == 0) {
                    border.setSize(border.getSize() - 25);
                    border.shrink();
                    event.broadcast("&6The border has been shrank to " + border.getSize() + "x" + border.getSize());
                    timeToShrink = 90;
                }

                if (timeToShrink % 60 == 0 && timeToShrink < 5) {
                    getPlayers().forEach(profile -> {
                        try {
                            PlayerUtil.playSound(profile.getPlayer(), "random.click");
                        } catch (Exception ignored) {}
                    });
                    event.broadcast("&6The border shrinks in &f" + timeToShrink + "s");
                }

                if (border.getSize() > 25) {
                    timeToShrink--;
                }

                super.tick();
            }
        }
    }

    @Override
    public void destroy() {
        border.destroy();
        scenarioManager.destroy();
    }

    public void delete() {
        border.destroy();
        scenarioManager.destroy();
        super.delete();
    }

    private Position getRandomPosition() {
        final int size = border.getSize();
        final int x = Utils.randomInteger(-size, size);
        final int z = Utils.randomInteger(-size, size);

        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            world.loadChunk(x >> 4, z >> 4);
        }

        Position position = new Position(x, world.getHighestBlockAt(x, z), z, world);

        Block block = world.getBlock(position.subtract(0, 1));
        if (block.getId() == Block.STILL_LAVA || block.getId() == Block.STILL_WATER) {
            getRandomPosition();
        }

        return position;
    }

    public void checkWinner() {
        if (currentState.equals(EventState.ENDING)) return;

        if ((!event.isTeam() && getPlayers().size() <= 1) || (event.isTeam() && event.getTeamManager().getTeamsAlive().size() <= 1)) stop();
    }

}
