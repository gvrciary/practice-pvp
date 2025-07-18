package alexis.practice.party.games;

import alexis.practice.Practice;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.world.DeleteWorldAsync;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PartyGame {
    protected int id;
    protected PartyGameState currentState = PartyGameState.STARTING;

    protected int startTime = 5;
    protected int runTime = 0;
    protected int endTime = 5;

    protected Level world;
    protected Kit kit;
    protected DuelWorld worldData;

    protected final List<String> blockData = new ArrayList<>();
    protected final List<String> spectators = new ArrayList<>();

    public void tick() {
        switch (currentState) {
            case STARTING -> {
                if (startTime <= 0) {
                    start();
                    return;
                }

                broadcast("&6" + startTime + "...");
                startTime--;
            }
            case RUNNING -> runTime++;
        }
    }

    public void setup() {}

    public boolean isLimitY(int y) {
        return (this.worldData.getFirstPosition().getFloorY() + 6) <= y;
    }

    public void start() {}

    public void removeBlock(Block block) {
        blockData.remove(block.getLocation().toString());
    }

    public List<Profile> getSpectators() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.spectators.contains(profile.getIdentifier()) &&
                        profile.isOnline())
                .collect(Collectors.toList());
    }

    public boolean isBlock(Block block) {
        Location blockPosition = block.getLocation();
        return blockPosition.getLevel().getFolderName().equals(getWorld().getFolderName()) &&
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

            player.setGamemode(Player.SPECTATOR);
            player.teleport(world.getBlock(worldData.getFirstPosition()).getLocation());

            spectators.add(profile.getIdentifier());

            if (profile.getProfileData().getParty() == null) {
                profile.getProfileData().setSpectate(this);
                player.sendMessage(TextFormat.colorize("&aType /hub to exit spectator mode"));
                broadcast("&a" + profile.getName() + " has joined as a spectator");

                player.getLevel().getPlayers().values().forEach(player::showPlayer);
            }
        } catch (Exception ignored) {}
    }

    public void removeSpectator(Profile profile) {
        spectators.remove(profile.getIdentifier());
        broadcast("&c" + profile.getName() + " has logged out as a spectator");

        if (profile.getProfileData().getParty() == null) {
            profile.getProfileData().setSpectate();
        }
    }

    public void broadcast(String message) {}

    public void checkWinner() {}

    public List<String> scoreboard(Profile profile) {
        return new ArrayList<>();
    }

    public void delete() {
        String worldName = world.getName();
        String path = Practice.getInstance().getServer().getDataPath() + File.separator + "worlds";

        PartyGamesManager.getInstance().removeDuel(id);
        Practice.getInstance().getServer().unloadLevel(world);
        Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new DeleteWorldAsync(worldName, path));
    }

}
