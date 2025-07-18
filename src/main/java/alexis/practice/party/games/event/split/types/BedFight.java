package alexis.practice.party.games.event.split.types;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.party.games.event.split.PartySplit;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedFight extends PartySplit {
    private static final int LIMIT_DURATION = 15 * 60;
    private static final int COOLDOWN = 3;

    private final Map<String, Integer> deathCooldown = new HashMap<>();

    private boolean firstBed = true, secondBed = true;

    public BedFight(int id, Party party, DuelWorld worldData, Level level, Kit kit) {
        super(id, party, worldData, level, kit);
    }

    public boolean isYourBed(Profile profile) {
        try {
            double bedFirstDistance = calculateDistance(worldData.getFirstPosition(), profile.getPlayer().getLocation());
            double bedSecondDistance = calculateDistance(worldData.getSecondPosition(), profile.getPlayer().getLocation());

            if (isFirstTeam(profile)) {
                return bedFirstDistance < bedSecondDistance;
            }

            return bedSecondDistance < bedFirstDistance;
        } catch (Exception ignored) {}
        return false;
    }

    public void updateDeathCooldown() {
         party.getMembers().forEach(profile -> {
             if (!profile.isOnline()) {
                 deathCooldown.remove(profile.getIdentifier());
                 return;
             }

             if (!deathCooldown.containsKey(profile.getIdentifier())) return;

             int seconds = deathCooldown.get(profile.getIdentifier());
             if (seconds == 0) {
                 getKit(profile);
                 try {
                     profile.getPlayer().teleport(world.getBlock(worldData.getFirstPosition()));
                 } catch (Exception ignored) {}

                 deathCooldown.remove(profile.getIdentifier());
             }

             try {
                 profile.getPlayer().sendTitle(TextFormat.colorize("&cYOU DIED"), TextFormat.colorize("&fRespawning in " + seconds));
             } catch (Exception ignored) {}

             deathCooldown.put(profile.getIdentifier(), seconds);
        });
    }

    public void setBrokenBed(Profile profile) {
        if (isFirstTeam(profile)) firstBed = false;
        else secondBed = false;

        getOpponentTeam(profile).forEach(s -> {
            try {
                Player player = s.getPlayer();

                player.sendTitle(TextFormat.colorize("&cBED DESTROYED"), TextFormat.colorize("&fYou will no longer respawn"));
                PlayerUtil.playSound(player, "mob.wither.death");
            } catch (Exception ignored) {}
        });
    }

    public void getKit(Profile profile) {
        super.getKit(profile);

        try {
            Player player = profile.getPlayer();

            player.getInventory().getContents().forEach((slot, item) -> {
                if (item.getId() == BlockID.WOOL) {
                    if (isFirstTeam(profile)) {
                        Item wood = Item.get(BlockID.WOOL, 4);
                        wood.setCount(64);
                        player.getInventory().setItem(slot, wood);
                    } else {
                        Item wood = Item.get(BlockID.WOOL, 14);
                        wood.setCount(64);
                        player.getInventory().setItem(slot, wood);
                    }
                }
            });

            player.setHealth(player.getMaxHealth());
        } catch (Exception ignored) {}
    }

    public void setDeath(Profile profile) {
        try {
            profile.getPlayer().setGamemode(Player.SPECTATOR);
        } catch (Exception ignored) {}

        if (isFirstTeam(profile)) {
            if (!firstBed && getAlives(blueTeam) == 0) {
                Profile lastHit = profile.getCacheData().getCombat().get();
                if (lastHit != null) profile.setDeathAnimation(lastHit);
                stop(redTeam);
                return;
            } else if (firstBed) deathCooldown.put(profile.getIdentifier(), COOLDOWN);
        } else {
            if (!secondBed && getAlives(redTeam) == 0) {
                Profile lastHit = profile.getCacheData().getCombat().get();
                if (lastHit != null) profile.setDeathAnimation(lastHit);
                stop(blueTeam);
                return;
            } else if (secondBed) deathCooldown.put(profile.getIdentifier(), COOLDOWN);
        }

        profile.clear();
        Profile lastHit = profile.getCacheData().getCombat().get();
        if (lastHit != null) {
            broadcast("&6" + profile.getName() + " &7killed by&6 " + lastHit.getName());
            increaseOpponentKills(profile);

            profile.getCacheData().getCombat().clear();
            return;
        }

        broadcast("&c" + profile.getName() + " has dead");
    }


    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();
        if (currentState.equals(PartyGameState.RUNNING)) {
            if (isSpectator(profile)) {
                lines.add("&l&6|&r &9[B]&f Blue: " + ((firstBed) ? "&aO" : "&cO"));
                lines.add("&l&6|&r &c[R]&f Red: " + ((secondBed) ? "&aO" : "&cO"));
                lines.add("&r&c");
                lines.add("&l&6|&r &fDuration: &6" + Utils.formatTime(runTime));
                lines.add("&f&r");
                lines.add("&l&6|&r &fBlue kills: &6" + firstKills);
                lines.add("&l&6|&r &fRed kills: &6" + secondKills);

                return lines;
            }

            lines.add("&l&6|&r &9[B]&f Blue: " + ((firstBed) ? "&aO" : "&cO") + ((isFirstTeam(profile)) ? "&7 YOU" : ""));
            lines.add("&l&6|&r &c[R]&f Red: " + ((secondBed) ? "&aO" : "&cO") + ((!isFirstTeam(profile)) ? "&7 YOU" : ""));
            lines.add("&f&r");
            lines.add("&l&6|&r &fYour kills: &6" + getKills(profile));
            lines.add("&l&6|&r &fTheir kills: &6" + getOpponentKills(profile));
            return lines;
        }

        return super.scoreboard(profile);
    }

    public void tick() {
        super.tick();

        if (currentState.equals(PartyGameState.RUNNING)) {
            if (LIMIT_DURATION == runTime) {
                stop(null);
                return;
            }

            updateDeathCooldown();
        }
    }

    private int getAlives(List<String> team) {
        return (int) getTeam(team).stream()
                .filter(profile -> {
                    try {
                        return profile.getPlayer().getGamemode() == Player.SURVIVAL && deathCooldown.getOrDefault(profile.getIdentifier(), 0) == 0;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .count();
    }
    private double calculateDistance(Vector3 firstLocation, Vector3 secondLocation) {
        return firstLocation.distance(secondLocation);
    }
}
