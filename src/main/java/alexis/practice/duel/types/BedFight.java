package alexis.practice.duel.types;

import alexis.practice.duel.Duel;
import alexis.practice.duel.DuelState;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
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
import java.util.List;

public class BedFight extends Duel {
    private static final int LIMIT_DURATION = 15 * 60;
    private static final int COOLDOWN = 3;

    private int firstCooldown = -1, secondCooldown = -1;
    private boolean firstBed = true, secondBed = true;
    private int firstKills = 0, secondKills = 0;

    public BedFight(int id, Profile firstProfile, Profile secondProfile, Level world, DuelWorld worldData, Kit kit, boolean ranked, int limit, boolean isDuel) {
        super(id, firstProfile, secondProfile, world, worldData, kit, ranked, limit, isDuel);
    }

    public boolean isYourBed(Profile profile) {
        try {
            double bedFirstDistance = calculateDistance(worldData.getFirstPosition(), profile.getPlayer().getLocation());
            double bedSecondDistance = calculateDistance(worldData.getSecondPosition(), profile.getPlayer().getLocation());

            if (isFirstProfile(profile)) {
                return bedFirstDistance < bedSecondDistance;
            }

            return bedSecondDistance < bedFirstDistance;
        } catch (Exception ignored) {}

        return false;
    }

    public void setBrokenBed(Profile profile) {
        Profile opponent = getOpponentProfile(profile);

        if (isFirstProfile(opponent)) firstBed = false;
        else secondBed = false;

        try {
            Player player = opponent.getPlayer();

            player.sendTitle(TextFormat.colorize("&cBED DESTROYED"), TextFormat.colorize("&fYou will no longer respawn"));
            PlayerUtil.playSound(player, "mob.wither.death");
        } catch (Exception ignored) {}
    }

    public int getKills(Profile profile) {
        if (isFirstProfile(profile)) {
            return firstKills;
        }

        return secondKills;
    }

    public void getKit(Profile profile) {
        super.getKit(profile);

        try {
            Player player = profile.getPlayer();

            player.getInventory().getContents().forEach((slot, item) -> {
                if (item.getId() == BlockID.WOOL) {
                    if (isFirstProfile(profile)) {
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
        if (isFirstProfile(profile)) {
            if (!firstBed) {
                profile.setDeathAnimation(secondProfile);
                stop(secondProfile);
                return;
            } else firstCooldown = COOLDOWN;
        } else {
            if (!secondBed) {
                profile.setDeathAnimation(firstProfile);
                stop(firstProfile);
                return;
            } else secondCooldown = COOLDOWN;
        }

        profile.clear();

        try {
            profile.getPlayer().setGamemode(Player.SPECTATOR);
        } catch (Exception ignored) {}

        Profile lastHit = profile.getCacheData().getCombat().get();
        if (lastHit != null) {
            broadcast("&6" + profile.getName() + " &7killed by&6 " + lastHit.getName());
            if (isFirstProfile(profile)) secondKills++;
            else firstKills++;

            profile.getCacheData().getCombat().clear();
            return;
        }

        broadcast("&c" + profile.getName() + " has dead");
    }


    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();
        if (state.equals(DuelState.RUNNING)) {
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

            Profile opponent = getOpponentProfile(profile);
            lines.add("&l&6|&r &9[B]&f Blue: " + ((firstBed) ? "&aO" : "&cO") + ((firstProfile.getIdentifier().equals(profile.getIdentifier())) ? "&7 YOU" : ""));
            lines.add("&l&6|&r &c[R]&f Red: " + ((secondBed) ? "&aO" : "&cO") + ((secondProfile.getIdentifier().equals(profile.getIdentifier())) ? "&7 YOU" : ""));
            lines.add("&f&r");
            lines.add("&l&6|&r &fYour kills: &6" + getKills(profile));
            lines.add("&r&r");
            try {
                lines.add("&l&6|&r &fYour ping: &6" + profile.getPlayer().getPing() + "ms");
                lines.add("&l&6|&r &fTheir ping: &6" + opponent.getPlayer().getPing() + "ms");
            } catch (Exception ignored) {}

            return lines;
        }

        return super.scoreboard(profile);
    }

    public void tick() {
        super.tick();

        if (state.equals(DuelState.RUNNING)) {
            if (LIMIT_DURATION == runTime) {
                stop();
                return;
            }

            if (firstCooldown == 0) {
                getKit(firstProfile);
                try {
                    firstProfile.getPlayer().teleport(world.getBlock(worldData.getFirstPosition()));
                } catch (Exception ignored) {}
            }

            if (secondCooldown == 0) {
                getKit(secondProfile);
                try {
                    secondProfile.getPlayer().teleport(world.getBlock(worldData.getSecondPosition()));
                } catch (Exception ignored) {}
            }

            if (--firstCooldown > -1) {
                try {
                    firstProfile.getPlayer().sendTitle(TextFormat.colorize("&cYOU DIED"), TextFormat.colorize("&fRespawning in " + firstCooldown));
                } catch (Exception ignored) {}
            }

            if (--secondCooldown > -1) {
                try {
                    secondProfile.getPlayer().sendTitle(TextFormat.colorize("&cYOU DIED"), TextFormat.colorize("&fRespawning in " + secondCooldown));
                } catch (Exception ignored) {}
            }
        }
    }

    private double calculateDistance(Vector3 firstLocation, Vector3 secondLocation) {
        return firstLocation.distance(secondLocation);
    }

    public void stop(Profile profile) {
        super.stop(profile);

        firstProfile.getCacheData().getCombat().clear();
        secondProfile.getCacheData().getCombat().clear();
    }
}
