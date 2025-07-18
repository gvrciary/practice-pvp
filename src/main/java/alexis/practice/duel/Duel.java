package alexis.practice.duel;

import alexis.practice.Practice;
import alexis.practice.division.DivisionManager;
import alexis.practice.duel.statistic.DuelStatistic;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.item.HotbarItem;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.EloUtil;
import alexis.practice.util.Fireworks;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import alexis.practice.util.world.DeleteWorldAsync;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Duel {

    protected int id;

    protected DuelState state = DuelState.STARTING;

    protected int startTime = 5;
    protected int runTime = 0;
    protected int endTime = 5;

    protected Profile firstProfile;
    protected Profile secondProfile;

    protected boolean firstRematch = false;
    protected boolean secondRematch = false;

    protected boolean isDuel;

    protected String winner;

    protected boolean canDropItem;

    protected int limit;
    protected int firstRounds = 0;
    protected int secondRounds = 0;

    protected Kit kit;
    protected boolean ranked;

    protected Level world;
    protected DuelWorld worldData;

    protected Map<Integer, Item> firstInventory;
    protected Map<Integer, Item>secondInventory;

    protected DuelStatistic duelStatistic;

    protected final List<String> blockData = new ArrayList<>();
    protected final List<String> spectators = new ArrayList<>();

    public Duel(int id, Profile firstProfile, Profile secondProfile, Level world, DuelWorld worldData, Kit kit, boolean ranked, int limit, boolean isDuel) {
        this.firstProfile = firstProfile;
        this.secondProfile = secondProfile;

        this.id = id;
        this.world = world;
        this.worldData = worldData;
        this.kit = kit;

        this.ranked = ranked;
        this.limit = limit;
        this.isDuel = isDuel;

        canDropItem = worldData.isCanDrop();
        duelStatistic = new DuelStatistic(this);

        firstProfile.getProfileData().setDuel(this);
        secondProfile.getProfileData().setDuel(this);

        setup();
    }

    public Duel() {}

    public boolean isLimitY(int y) {
        return (this.worldData.getFirstPosition().getFloorY() + 6) <= y;
    }

    public void setRematch (Profile profile) {
        if (isFirstProfile(profile)) {
            firstRematch = true;
            return;
        }

        secondRematch = true;
    }

    public boolean isBestOf() {
        return limit > 1;
    }

    public void setup() {
        if (!firstProfile.isOnline() || !secondProfile.isOnline()) {
            stop();
            return;
        }

        firstProfile.getCacheData().clearCooldown();
        secondProfile.getCacheData().clearCooldown();

        if (ranked) {
            firstProfile.getStatisticsData().increaseMatches();
            secondProfile.getStatisticsData().increaseMatches();
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
                    firstPlayer.sendMessage(TextFormat.colorize("&6Match Found in "+ kit.getCustomName() +"\n&6-&f Rival:&6 " + secondProfile.getName() +  " &7(" + DivisionManager.getInstance().getDivisionByElo(secondProfile.getStatisticsData().getElo(kit.getName())).getRankFormat() + ")" + "\n&6-&f Elo:&6 " + secondProfile.getStatisticsData().getElo(kit.getName()) + "\n&6-&f Ping:&6 " + secondPlayer.getPing() + "ms"));
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
                    secondPlayer.sendMessage(TextFormat.colorize("&6Match Found in "+ kit.getCustomName() +"\n&6-&f Rival:&6 " + firstProfile.getName() +  " &7(" + DivisionManager.getInstance().getDivisionByElo(firstProfile.getStatisticsData().getElo(kit.getName())).getRankFormat() + ")" + "\n&6-&f Elo:&6 " + firstProfile.getStatisticsData().getElo(kit.getName()) + "\n&6-&f Ping:&6 " + firstPlayer.getPing() + "ms"));
                }
            }
        } catch (Exception ignored) {}
    }

    public void getKit(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();

            profile.clear();
            player.setGamemode(Player.SURVIVAL);
            player.setMaxHealth(player.getMaxHealth());

            kit.giveKit(profile);
        } catch (Exception ignored) {}
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

    protected int getRounds(Profile profile) {
        if (isFirstProfile(profile)) {
            return firstRounds;
        }
        return secondRounds;
    }

    public void setDeath(Profile profile) {
        if (isBestOf()) {
            if (isFirstProfile(profile)) {
                secondRounds++;

                if (secondRounds >= limit) {
                    profile.setDeathAnimation(secondProfile);
                    stop(secondProfile);
                    return;
                }

            } else {
                firstRounds++;

                if (firstRounds >= limit) {
                    profile.setDeathAnimation(firstProfile);
                    stop(firstProfile);
                    return;
                }
            }

            startTime = 5;
            state = DuelState.STARTING;

            setup();
            return;
        }

        Profile opponent = getOpponentProfile(profile);
        profile.setDeathAnimation(opponent);
        stop(opponent);
    }

    public void tick() {
        switch (state) {
            case STARTING -> {
                if (startTime <= 0) {
                    start();
                    return;
                }

                broadcast("&6" + startTime + "...");

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

                startTime--;
            }
            case RUNNING -> runTime++;
            case ENDING -> {
                if (--endTime <= 0) {
                    DuelType type = DuelType.get(kit.getName());

                    if (!firstRematch) {
                        firstProfile.clear();
                        firstProfile.getProfileData().setDuel();

                        if (firstProfile.isOnline()) {
                            try {
                                Player player = firstProfile.getPlayer();

                                PlayerUtil.getLobbyKit(player);

                                if (secondProfile.isOnline()) {
                                    Player secondPlayer = secondProfile.getPlayer();
                                    player.showPlayer(secondPlayer);
                                }

                                if (secondInventory != null) {
                                    PlayerUtil.sendFinishForm(firstProfile, secondProfile, type, ranked, duelStatistic.toString(), secondInventory);
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    if (!secondRematch) {
                        secondProfile.clear();
                        secondProfile.getProfileData().setDuel();

                        if (secondProfile.isOnline()) {
                            try {
                                Player player = secondProfile.getPlayer();

                                PlayerUtil.getLobbyKit(player);

                                if (firstProfile.isOnline()) {
                                    Player firstPlayer = firstProfile.getPlayer();
                                    player.showPlayer(firstPlayer);
                                }

                                if (firstInventory != null) {
                                    PlayerUtil.sendFinishForm(secondProfile, firstProfile, type, ranked, duelStatistic.toString(), firstInventory);
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    getSpectators().stream().filter(profile -> profile.getProfileData().isSpectator()).forEach(spectator -> spectator.getProfileData().setSpectate());

                    delete();
                }
            }
        }
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();
        switch (state) {
            case STARTING, RUNNING -> {
                if (isSpectator(profile)) {
                    try {
                        lines.add(" &r&f" + firstProfile.getName() + " &7(" + firstProfile.getPlayer().getPing() + ")");
                        lines.add(" &r&6VS");
                        lines.add(" &r&f" + secondProfile.getName() + " &7(" + secondProfile.getPlayer().getPing() + ")");
                    } catch (Exception ignored) {}

                    lines.add("&l&6|&r&f Duration:&6 " + Utils.formatTime(runTime));
                    lines.add("&l&6|&r&f Mode: &6" + (ranked ? "Ranked" : "Unranked") + " : " + kit.getCustomName());

                    if (isBestOf()) {
                        lines.add(" &6&r");
                        lines.add("&l&6|&r&6 Rounds Info:");
                        lines.add(" &f" + firstProfile.getName() + "&7(" + firstRounds + ")");
                        lines.add(" &f" + secondProfile.getName() + "&7(" + secondRounds + ")");
                    }
                    return lines;
                }

                Profile opponent = getOpponentProfile(profile);

                if (state.equals(DuelState.STARTING)) {
                    lines.add("&l&6|&r&f Opponent:&6 " + opponent.getName());
                    lines.add("&r&4");
                }

                try {
                    lines.add("&l&6|&r &fYour Ping:&6 " + profile.getPlayer().getPing() + "ms");
                    lines.add("&l&6|&r &fTheir Ping:&6 " + opponent.getPlayer().getPing() + "ms");
                } catch (Exception ignored) {}

                if (state.equals(DuelState.STARTING) && isBestOf()) {
                    lines.add("&r&5");
                    lines.add("&l&6|&r&f Your Rounds:&6 " + getRounds(profile));
                    lines.add("&l&6|&r&f Their Rounds:&6 " + getRounds(opponent));
                }
            }

            case ENDING -> {
                if (winner != null && winner.equals(profile.getIdentifier())) {
                    lines.add("&l&6|&a VICTORY");
                    lines.add("&r&9");
                } else if (winner != null && !isSpectator(profile)) {
                    lines.add("&l&6|&c DEFEAT");
                    lines.add("&r&9");
                }

                if (isBestOf()) {
                    lines.add("&r&c");
                    lines.add("&l&6|&r&f Score:&6 " + firstRounds + "-" + secondRounds);
                    lines.add("&r&9");
                }

                lines.add("&l&6|&r&f Duration:&6 " + Utils.formatTime(runTime));
            }
        }

        return lines;
    }

    public List<Profile> getSpectators() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.spectators.contains(profile.getIdentifier()) &&
                        profile.isOnline())
                .collect(Collectors.toList());
    }

    public void stop() {
        this.stop(null, true);
    }

    public void stop(@Nullable Profile profile) {
        this.stop(profile, false);
    }

    public void stop(@Nullable Profile profile, boolean forceStop) {
        state = DuelState.ENDING;

        if (profile != null) {
            Profile winner = (isFirstProfile(profile) ? firstProfile : secondProfile);
            Profile loser = (isFirstProfile(winner) ? secondProfile : firstProfile);

            this.winner = winner.getIdentifier();

            if (winner.isOnline()) {
                try {
                    Player winnerPlayer = winner.getPlayer();
                    Fireworks.spawnFirework(winnerPlayer);

                    if (loser.isOnline()) {
                        winnerPlayer.hidePlayer(loser.getPlayer());
                    }
                } catch (Exception ignored) {}
            }

            broadcast("&l&6Match Results\n&r&aWinner:&f " + winner.getName() + "&8 |&c Loser:&f " + loser.getName());

            String spectators = getSpectators().stream()
                    .filter(s -> s.getProfileData().isSpectator())
                    .map(Profile::getName)
                    .collect(Collectors.joining(", "));

            if (!spectators.isEmpty()) {
                broadcast("&aSpectators: " + spectators);
            }

            if (firstProfile.isOnline() && secondProfile.isOnline()) {
                try {
                    firstInventory = firstProfile.getPlayer().getInventory().getContents();
                    secondInventory = secondProfile.getPlayer().getInventory().getContents();
                } catch (Exception ignored) {}
            }

            if (ranked) {
                winner.getStatisticsData().increaseKills();
                winner.getStatisticsData().increaseWins();
                loser.getStatisticsData().increaseDeaths();

                EloUtil.setElo(loser, profile, DuelType.get(kit.getName()));
            }
        }

        if (forceStop) {
            delete();
        }

        firstProfile.clear();
        secondProfile.clear();

        if (firstProfile.isOnline()) {
            try {
                firstProfile.getPlayer().setImmobile(false);
            } catch (Exception ignored) {}
        }

        if (secondProfile.isOnline()) {
            try {
                secondProfile.getPlayer().setImmobile(false);
            } catch (Exception ignored) {}
        }

        if (!isDuel) {
            if (firstProfile.isOnline()) {
                try {
                    firstProfile.getPlayer().getInventory().setItem(0, HotbarItem.REQUEST.getItem());
                } catch (Exception ignored) {}
            }

            if (secondProfile.isOnline()) {
                try {
                    secondProfile.getPlayer().getInventory().setItem(0, HotbarItem.REQUEST.getItem());
                } catch (Exception ignored) {}
            }
        }

        firstProfile.getCacheData().clearCooldown();
        secondProfile.getCacheData().clearCooldown();
    }

    public void start() {
        if (!firstProfile.isOnline() || !secondProfile.isOnline()) {
            stop();
            return;
        }

        state = DuelState.RUNNING;

        try {
            if (firstProfile.isOnline()) {
                Player firstPlayer = firstProfile.getPlayer();
                firstPlayer.setImmobile(false);

                PlayerUtil.playSound(firstPlayer, "");
            }

            if (secondProfile.isOnline()) {
                Player secondPlayer = secondProfile.getPlayer();
                secondPlayer.setImmobile(false);
            }
        } catch (Exception ignored) {}

        broadcast("&aMatch started");
    }

    protected void delete() {
        String worldName = world.getName();
        String path = Practice.getInstance().getServer().getDataPath() + File.separator + "worlds";

        Practice.getInstance().getServer().unloadLevel(world);
        Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new DeleteWorldAsync(worldName, path));

        DuelManager.getInstance().removeDuel(id);
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

            player.setGamemode(Player.SPECTATOR);
            player.teleport(world.getBlock(worldData.getFirstPosition()).getLocation());

            spectators.add(profile.getIdentifier());

            profile.getProfileData().setSpectate(this);
            broadcast("&7" + profile.getName() + " &6has joined as a spectator");

            player.getLevel().getPlayers().values().forEach(player::showPlayer);
        } catch (Exception ignored) {}
    }

    public void removeSpectator(Profile profile) {
        spectators.remove(profile.getIdentifier());
        profile.getProfileData().setSpectate();
        broadcast("&c" + profile.getName() + " has logged out as a spectator");
    }

    protected void broadcast(String message) {
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

   public Profile getOpponentProfile(Profile profile) {
        if (profile.getIdentifier().equals(firstProfile.getIdentifier())) {
            return secondProfile;
        }
        return firstProfile;
   }

   protected boolean isFirstProfile(Profile profile) {
        return firstProfile.getIdentifier().equals(profile.getIdentifier());
   }

}
