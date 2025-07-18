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
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bridge extends Duel {
    private static final int LIMIT_DURATION = 15 * 60;

    private final Map<String, Integer> arrowCooldown = new HashMap<>();
    private int firstKills = 0, secondKills = 0;
    private int firstPoints = 0, secondPoints = 0;

    private final int STATUS_STARTING = 0;
    private final int STATUS_RUNNING = 1;

    private int BATTLE_STATUS = STATUS_RUNNING;

    public Bridge(int id, Profile firstProfile, Profile secondProfile, Level world, DuelWorld worldData, Kit kit, boolean ranked, int limit, boolean isDuel) {
        super(id, firstProfile, secondProfile, world, worldData, kit, ranked, limit, isDuel);
    }

    public void addArrowCooldown(Profile profile) {
        arrowCooldown.put(profile.getIdentifier(), 5);
    }

    public void updateArrowCooldown(Profile profile) {
        if (!profile.isOnline()) {
            arrowCooldown.remove(profile.getIdentifier());
            return;
        }

        if (!arrowCooldown.containsKey(profile.getIdentifier())) return;

        int seconds = arrowCooldown.get(profile.getIdentifier());

        if (seconds == 0) {
            arrowCooldown.remove(profile.getIdentifier());

            try {
                Player player = profile.getPlayer();

                player.setExperience(0, 0);
                int count = Utils.countItems(player.getInventory().getContents().values(), ItemID.ARROW);
                if (count == 0) player.getInventory().addItem(Item.get(ItemID.ARROW));
            } catch (Exception ignored) {}
            return;
        }

        arrowCooldown.put(profile.getIdentifier(), seconds - 1);

        try {
            PlayerUtil.setExperience(profile.getPlayer(), seconds, seconds, 5);
        } catch (Exception ignored) {}
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        if (state.equals(DuelState.RUNNING)) {
            if (BATTLE_STATUS == STATUS_RUNNING || BATTLE_STATUS == STATUS_STARTING) {
                int firstPoints = this.firstPoints;
                int secondPoints = this.secondPoints;

                if (isSpectator(profile)) {
                    lines.add("&l&6|&r &9[B] " + Utils.repeat("|", firstPoints) + "&7" + Utils.repeat("|", 5 - firstPoints));
                    lines.add("&l&6|&r &c[R] " + Utils.repeat("|", secondPoints) + "&7" + Utils.repeat("|", 5 - secondPoints));
                    lines.add("&r&c");
                    lines.add("&l&6|&r &fDuration: &6" + Utils.formatTime(runTime));
                    lines.add("&f&r");
                    lines.add("&l&6|&r &fBlue kills: &6" + firstKills);
                    lines.add("&l&6|&r &fRed kills: &6" + secondKills);

                    return lines;
                }

                Profile opponent = getOpponentProfile(profile);

                lines.add("&l&6|&r &9[B] " + Utils.repeat("|", firstPoints) + "&7" + Utils.repeat("|", 5 - firstPoints));
                lines.add("&l&6|&r &c[R] " + Utils.repeat("|", secondPoints) + "&7" + Utils.repeat("|", 5 - secondPoints));
                lines.add("&r&c");
                lines.add("&l&6|&r &fYour kills: &6" + getKills(profile));
                lines.add("&r&r ");
                try {
                    lines.add("&l&6|&r &fYour ping: &6" + profile.getPlayer().getPing() + "ms");
                    lines.add("&l&6|&r &fTheir ping: &6" + opponent.getPlayer().getPing() + "ms");
                } catch (Exception ignored) {}

                return lines;
            }
        }

        return super.scoreboard(profile);
    }

    public void stop(Profile profile) {
        super.stop(profile);

        firstProfile.getCacheData().getCombat().clear();
        secondProfile.getCacheData().getCombat().clear();
    }

    public void check() {
        if (firstPoints > secondPoints) {
            stop(firstProfile);
        } else if (secondPoints > firstPoints) {
            stop(secondProfile);
        } else {
            stop();
        }
    }

    public int getKills(Profile profile) {
        if (isFirstProfile(profile)) {
            return firstKills;
        }

        return secondKills;
    }

    public DuelWorld.Portal getPortal(Profile profile) {
        if (isFirstProfile(profile)) {
            return worldData.getPortalFirst();
        }
        return worldData.getPortalSecond();
    }

    public void setDeath(Profile profile) {
        arrowCooldown.remove(profile.getIdentifier());

        try {
            if (isFirstProfile(profile)) profile.getPlayer().teleport(world.getBlock(worldData.getFirstPosition()));
            else profile.getPlayer().teleport(world.getBlock(worldData.getSecondPosition()));
        } catch (Exception ignored) {}

        getKit(profile);

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

    public void addPoint(Profile profile) {
        if (isFirstProfile(profile)) {
            firstPoints++;
            if (firstPoints >= 5){
                stop(firstProfile);
                return;
            }
        } else {
            secondPoints++;
            if (secondPoints >= 5) {
                stop(secondProfile);
                return;
            }
        }

        startTime = 5;
        BATTLE_STATUS = STATUS_STARTING;

        try {
            firstProfile.getPlayer().teleport(world.getBlock(worldData.getFirstPosition()));
            secondProfile.getPlayer().teleport(world.getBlock(worldData.getSecondPosition()));
        } catch (Exception ignored) {}

        getKit(firstProfile);
        getKit(secondProfile);

        if (firstProfile.isOnline() && secondProfile.isOnline()) {
            String message = (isFirstProfile(profile) ? TextFormat.BLUE : TextFormat.RED) + profile.getName() + " scored!\n" + TextFormat.BLUE + firstPoints + " - " + TextFormat.RED + secondPoints;

            try {
                if (firstProfile.isOnline()) {
                    Player firstPlayer = firstProfile.getPlayer();
                    firstPlayer.sendTitle(TextFormat.colorize(message));
                    firstPlayer.setImmobile();

                    PlayerUtil.playSound(firstPlayer, "random.orb");
                }

                if (secondProfile.isOnline()) {
                    Player secondPlayer = secondProfile.getPlayer();
                    secondPlayer.setImmobile();
                    secondPlayer.sendTitle(TextFormat.colorize(message));

                    PlayerUtil.playSound(secondPlayer, "random.orb");
                }
            } catch (Exception ignored) {}
        }
    }

    public void getKit(Profile profile) {
        super.getKit(profile);

        try {
            Player player = profile.getPlayer();

            player.getInventory().getContents().forEach((slot, item) -> {
                if (item.getId() == BlockID.STAINED_TERRACOTTA) {
                    if (isFirstProfile(profile)) {
                        Item wood = Item.get(BlockID.STAINED_TERRACOTTA, 3);
                        wood.setCount(64);
                        player.getInventory().setItem(slot, wood);
                    } else {
                        Item wood = Item.get(BlockID.STAINED_TERRACOTTA, 14);
                        wood.setCount(64);
                        player.getInventory().setItem(slot, wood);
                    }
                }
            });

            player.setExperience(0, 0);
            player.setHealth(player.getMaxHealth());
        } catch (Exception ignored) {}
    }

    public void tick() {
        super.tick();

        if (state.equals(DuelState.RUNNING)) {
            if (BATTLE_STATUS == STATUS_STARTING) {
                if (--startTime <= 0){
                    BATTLE_STATUS = STATUS_RUNNING;

                    try {
                        firstProfile.getPlayer().setImmobile(false);
                        secondProfile.getPlayer().setImmobile(false);
                    } catch (Exception ignored) {}

                    return;
                }

                try {
                    firstProfile.getPlayer().sendTitle(TextFormat.colorize("&9" + startTime));
                    secondProfile.getPlayer().sendTitle(TextFormat.colorize("&c" + startTime));
                } catch (Exception ignored) {}
            } else if (BATTLE_STATUS == STATUS_RUNNING) {
                if (LIMIT_DURATION == runTime) {
                    check();
                    return;
                }

                if (arrowCooldown.containsKey(firstProfile.getIdentifier())) {
                    updateArrowCooldown(firstProfile);
                }

                if (arrowCooldown.containsKey(secondProfile.getIdentifier())) {
                    updateArrowCooldown(secondProfile);
                }

            }
        }
    }
}
