package alexis.practice.arena;

import alexis.practice.arena.world.ArenaWorld;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Arena {

    private final List<String> players = new ArrayList<>();
    private final List<String> spectators = new ArrayList<>();

    @Getter
    public final Map<Location, Long> blockCache = new HashMap<>();
    @Getter
    private final ArenaWorld arenaData;

    public Arena(ArenaWorld arenaData) {
        this.arenaData = arenaData;
    }

    public Kit getKit() {
        return arenaData.getKit();
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        if (isSpectator(profile)) {
            lines.add("&l&6|&r&f Arena: &6" + arenaData.getName());
            lines.add("&l&6|&r&f Playing: &6" + getPlayers().size());
            return lines;
        }

        try {
            lines.add("&l&6|&r&f Your Ping:&6 " + profile.getPlayer().getPing() + "ms");
        } catch (Exception ignored) {}

        Profile killer = profile.getCacheData().getCombat().get();

        if (killer != null && killer.isOnline()) {
            try {
                lines.add("&l&6|&r&f Their Ping: &6" + killer.getPlayer().getPing() + "ms");
            } catch (Exception ignored) {}
            lines.add("&8&r");
            lines.add("&l&6|&r&f Combat: &6" + Utils.formatTime(profile.getCacheData().getCombat().getTime() - System.currentTimeMillis()));
        } else {
            lines.add("&l&6|&r&f Playing: &6" + getPlayers().size());
        }

        return lines;
    }

    public void broadcast(String message) {
        getPlayers().forEach(profile -> {
            try {
                profile.getPlayer().sendMessage(TextFormat.colorize(message));
            } catch (Exception ignored) {}
        });
    }

    public List<Profile> getPlayers() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.players.contains(profile.getIdentifier()) &&
                        profile.isOnline() &&
                        profile.getProfileData().getArena() != null &&
                        profile.getProfileData().getArena().equals(this))
                .collect(Collectors.toList());
    }

    public void tick() {
        if (blockCache.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        blockCache.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                arenaData.getWorld().setBlock(entry.getKey(), Block.get(Block.AIR), true);
                return true;
            }
            return false;
        });
    }

    public void reset() {
        blockCache.entrySet().removeIf(entry -> {
            arenaData.getWorld().setBlock(entry.getKey(), Block.get(Block.AIR), true);
            return true;
        });
    }

    public void addBlock(Location location) {
        blockCache.put(location, System.currentTimeMillis() + 5000);
    }

    public boolean hasBlock(Location location) {
        return blockCache.containsKey(location);
    }

    public void removeBlock(Location location) {
        blockCache.remove(location);
    }

    public void addPlayer(Profile profile) {
        try {
            profile.clear();
            Player player = profile.getPlayer();

            profile.getCacheData().getCombat().clear();
            profile.getCacheData().clearCooldown();
            Vector3 spawn = arenaData.getSpawns().get(Utils.randomInteger(0, arenaData.getSpawns().size() - 1));
            player.teleport(arenaData.getWorld().getBlock(spawn));

            if (arenaData.isCanBuild()) {
                player.setGamemode(Player.SURVIVAL);
            }

            players.add(profile.getIdentifier());

            getKit().giveKit(profile);
            profile.getProfileData().setArena(this);

            player.getLevel().getPlayers().values().forEach(player::showPlayer);
        } catch (Exception ignored) {}
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
            Vector3 spawn = arenaData.getSpawns().get(Utils.randomInteger(0, arenaData.getSpawns().size() - 1));
            player.teleport(arenaData.getWorld().getBlock(spawn));

            spectators.add(profile.getIdentifier());

            profile.getProfileData().setSpectate(this);
            player.sendMessage(TextFormat.colorize("&aType /hub to exit spectator mode"));
            broadcast("&7" + profile.getName() + " &6has joined as a spectator");

            player.getLevel().getPlayers().values().forEach(player::showPlayer);
        } catch (Exception ignored) {}
    }

    public void removeSpectator(Profile profile) {
        spectators.remove(profile.getIdentifier());
        profile.getProfileData().setSpectate();
        broadcast("&c" + profile.getName() + " has logged out as a spectator");
    }

    public void removePlayer(Profile profile) {
        removePlayer(profile, true);
    }

    public void removePlayer(Profile profile, boolean forceLeave) {
        if (profile.getCacheData().getCooldownFFA() > 1) {
            profile.getCacheData().setCooldownFFA(0);

            removeDataPlayer(profile);
            return;
        }

        Profile killer = profile.getCacheData().getCombat().get();

        if (killer != null) {
            profile.getStatisticsData().increaseDeaths();
            profile.setDeathAnimation(killer);

            try {
                if (killer.isOnline()) {
                    Player player = profile.getPlayer();
                    Player targetPlayer = killer.getPlayer();

                    String killMessage = "was slain by";
                    if (killer.getCosmeticData().hasKillMessage()) {
                        killMessage = killer.getCosmeticData().getKillMessage().getFormat();
                    }

                    String message;
                    if (arenaData.getKit().getName().toLowerCase().contains("nodebuff")) {
                        message = "&e" + profile.getName() + " [" + Utils.countItems(player.getInventory().getContents().values(), ItemID.SPLASH_POTION) + "] &7" + killMessage + " &e" + killer.getName() + " [" + Utils.countItems(targetPlayer.getInventory().getContents().values(), ItemID.SPLASH_POTION) + "]";
                    } else message = "&e" + profile.getName() + " &7"+ killMessage +  " &e" + killer.getName();

                    broadcast(message);

                    targetPlayer.setHealth(targetPlayer.getMaxHealth());
                    arenaData.getKit().giveKit(killer);

                    killer.getCacheData().getCombat().clear();
                    killer.getCacheData().clearCooldown();

                    killer.getStatisticsData().increaseKills();
                }
            } catch (Exception ignored) {}
        }

        profile.getCacheData().clearCooldown();
        profile.getCacheData().getCombat().clear();

        if (forceLeave) {
            players.remove(profile.getIdentifier());
            profile.clear();
            profile.getProfileData().setArena();

            try {
                PlayerUtil.getLobbyKit(profile.getPlayer());
            } catch (Exception ignored) {}
        } else {
            try {
                profile.clear();
                profile.getPlayer().setGamemode(Player.SPECTATOR);
                profile.getCacheData().setCooldownFFA(3);
            } catch (Exception ignored) {}
        }
    }

    public void removeDataPlayer(Profile profile) {
        players.remove(profile.getIdentifier());
        profile.getProfileData().setArena();
    }

}
