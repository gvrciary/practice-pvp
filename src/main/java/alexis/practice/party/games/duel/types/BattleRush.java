package alexis.practice.party.games.duel.types;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.party.games.duel.PartyDuel;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleRush extends PartyDuel {
    private static final int LIMIT_DURATION = 15 * 60;

    @Getter
    public final Map<Location, Long> blockCache = new HashMap<>();

    private int firstPoints = 0, secondPoints = 0;

    private final int STATUS_STARTING = 0;
    private final int STATUS_RUNNING = 1;

    private int BATTLE_STATUS = STATUS_RUNNING;

    public BattleRush(int id, Party firstTeam, Party secondTeam, DuelWorld worldData, Level level, Kit kit) {
        super(id, firstTeam, secondTeam, worldData, level, kit);
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        if (currentState.equals(PartyGameState.RUNNING)) {
            if (BATTLE_STATUS == STATUS_RUNNING || BATTLE_STATUS == STATUS_STARTING) {
                int firstPoints = this.firstPoints;
                int secondPoints = this.secondPoints;

                if (isSpectator(profile) && profile.getProfileData().isSpectator()) {
                    lines.add("&l&6|&r &9[B] " + Utils.repeat("|", firstPoints) + "&7" + Utils.repeat("|", 5 - firstPoints));
                    lines.add("&l&6|&r &c[R] " + Utils.repeat("|", secondPoints) + "&7" + Utils.repeat("|", 5 - secondPoints));
                    lines.add("&r&c");
                    lines.add("&l&6|&r &fDuration: &6" + Utils.formatTime(runTime));
                    lines.add("&f&r");
                    lines.add("&l&6|&r &fBlue kills: &6" + getKills(firstTeam));
                    lines.add("&l&6|&r &fRed kills: &6" + getKills(secondTeam));

                    return lines;
                }

                Party party = profile.getProfileData().getParty();
                Party opponent = getOpponentParty(party);

                lines.add("&l&6|&r &9[B] " + Utils.repeat("|", firstPoints) + "&7" + Utils.repeat("|", 5 - firstPoints) + ((firstTeam.getId() == party.getId()) ? "&7 YOU" : ""));
                lines.add("&l&6|&r &c[R] " + Utils.repeat("|", secondPoints) + "&7" + Utils.repeat("|", 5 - secondPoints) + ((secondTeam.getId() == party.getId()) ? "&7 YOU" : ""));
                lines.add("&r&c");
                lines.add("&l&6|&r &fYour kills: &6" + getKills(profile.getProfileData().getParty()));
                lines.add("&l&6|&r &fTheir kills: &6" + getKills(opponent));
                return lines;
            }
        }

        return super.scoreboard(profile);
    }

    public void check() {
        if (firstPoints > secondPoints) {
            stop(firstTeam);
        } else if (secondPoints > firstPoints) {
            stop(secondTeam);
        } else {
            stop(null);
        }
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

    public void updateBlocks() {
        if (blockCache.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        blockCache.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                world.setBlock(entry.getKey(), Block.get(Block.AIR), true);
                return true;
            }
            return false;
        });
    }

    public DuelWorld.Portal getPortal(Party party) {
        if (isFirstParty(party)) {
            return worldData.getPortalFirst();
        }
        return worldData.getPortalSecond();
    }

    public void setDeath(Profile profile) {
        try {
            if (isFirstParty(profile.getProfileData().getParty())) profile.getPlayer().teleport(world.getBlock(worldData.getFirstPosition()));
            else profile.getPlayer().teleport(world.getBlock(worldData.getSecondPosition()));
        } catch (Exception ignored) {}

        getKit(profile);

        Profile lastHit = profile.getCacheData().getCombat().get();
        if (lastHit != null) {
            broadcast("&6" + profile.getName() + " &7killed by&6 " + lastHit.getName());

            if (isFirstParty(profile.getProfileData().getParty())) secondKills++;
            else firstKills++;

            profile.getCacheData().getCombat().clear();
            return;
        }

        broadcast("&c" + profile.getName() + " has dead");
    }

    public void addPoint(Profile profile) {
        if (isFirstParty(profile.getProfileData().getParty())) {
            firstPoints++;
            if (firstPoints >= 3){
                stop(firstTeam);
                return;
            }
        } else {
            secondPoints++;
            if (secondPoints >= 3) {
                stop(secondTeam);
                return;
            }
        }

        startTime = 5;
        BATTLE_STATUS = STATUS_STARTING;

        String message = (isFirstParty(profile.getProfileData().getParty()) ? TextFormat.BLUE : TextFormat.RED) + profile.getName() + " scored!\n" + TextFormat.BLUE + firstPoints + " - " + TextFormat.RED + secondPoints;

        firstTeam.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                player.teleport(world.getBlock(worldData.getFirstPosition()));
                getKit(member);

                player.setImmobile();
                player.sendTitle(TextFormat.colorize(message));

                PlayerUtil.playSound(player, "random.orb");
            } catch (Exception ignored) {}
        });

        secondTeam.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                player.teleport(world.getBlock(worldData.getSecondPosition()));
                getKit(member);

                player.setImmobile();
                player.sendTitle(TextFormat.colorize(message));

                PlayerUtil.playSound(player, "random.orb");
            } catch (Exception ignored) {}
        });
    }

    public void getKit(Profile profile) {
        super.getKit(profile);

        try {
            Player player = profile.getPlayer();

            player.getInventory().getContents().forEach((slot, item) -> {
                if (item.getId() == BlockID.STAINED_TERRACOTTA) {
                    if (isFirstParty(profile.getProfileData().getParty())) {
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

        if (currentState.equals(PartyGameState.RUNNING)) {
            if (BATTLE_STATUS == STATUS_STARTING) {
                if (--startTime <= 0){
                    BATTLE_STATUS = STATUS_RUNNING;


                    secondTeam.getMembers().forEach(member -> {
                        try {
                            member.getPlayer().setImmobile(false);
                        } catch (Exception ignored) {}
                    });

                    firstTeam.getMembers().forEach(member -> {
                        try {
                            member.getPlayer().setImmobile(false);
                        } catch (Exception ignored) {}
                    });
                    return;
                }

                secondTeam.getMembers().forEach(member -> {
                    try {
                        member.getPlayer().sendTitle(TextFormat.colorize("&c" + startTime));
                    } catch (Exception ignored) {}
                });

                firstTeam.getMembers().forEach(member -> {
                    try {
                        member.getPlayer().sendTitle(TextFormat.colorize("&9" + startTime));
                    } catch (Exception ignored) {}
                });

            } else if (BATTLE_STATUS == STATUS_RUNNING) {
                if (LIMIT_DURATION == runTime) {
                    check();
                    return;
                }

                updateBlocks();
            }
        }
    }
}
