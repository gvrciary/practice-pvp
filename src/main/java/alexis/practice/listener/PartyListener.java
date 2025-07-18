package alexis.practice.listener;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGame;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.party.games.PartyGamesManager;
import alexis.practice.party.games.duel.PartyDuel;
import alexis.practice.party.games.duel.types.BattleRush;
import alexis.practice.party.games.duel.types.Boxing;
import alexis.practice.party.games.duel.types.Bridge;
import alexis.practice.party.games.event.split.PartySplit;
import alexis.practice.party.games.event.split.types.BedFight;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.inventory.InventoryPickupArrowEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PartyListener implements Listener {

    @EventHandler
    public void handleBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Block block = event.getBlock();
        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();

        if (!duel.getCurrentState().equals(PartyGameState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (block.getId() == Block.OBSIDIAN) return;

        if (duel instanceof PartyDuel) {
            if (duel instanceof alexis.practice.party.games.duel.types.BedFight bedFight) {
                if (block.getId() == BlockID.BED_BLOCK) {
                    if (bedFight.isYourBed(profile)) {
                        event.setCancelled();
                        return;
                    }

                    bedFight.setBrokenBed(profile);
                    event.setDrops(new Item[]{});
                    return;
                }

                if (block.getId() == BlockID.END_STONE || block.getId() == 5) {
                    event.setDrops(new Item[]{});
                    return;
                }
            } else if (duel instanceof BattleRush battleRush) {
                if (battleRush.hasBlock(block.getLocation())) {
                    battleRush.removeBlock(block.getLocation());
                    event.setDrops(new Item[0]);
                } else {
                    event.setCancelled();
                }

                return;
            }
        } else if (duel instanceof PartySplit) {
            if (duel instanceof BedFight bedFight) {
                if (block.getId() == BlockID.BED_BLOCK) {
                    if (bedFight.isYourBed(profile)) {
                        event.setCancelled();
                        return;
                    }

                    bedFight.setBrokenBed(profile);
                    event.setDrops(new Item[]{});
                    return;
                }

                if (block.getId() == BlockID.END_STONE || block.getId() == 5) {
                    event.setDrops(new Item[]{});
                    return;
                }
            } else if (duel instanceof alexis.practice.party.games.event.split.types.BattleRush battleRush) {
                if (battleRush.hasBlock(block.getLocation())) {
                    battleRush.removeBlock(block.getLocation());
                    event.setDrops(new Item[0]);
                } else {
                    event.setCancelled();
                }

                return;
            }
        }

        if (!duel.isBlock(block)) {
            event.setCancelled();
            return;
        }

        duel.removeBlock(block);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handlePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Block block = event.getBlock();
        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();

        if (!duel.getCurrentState().equals(PartyGameState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (duel.isLimitY(block.getLocation().getFloorY())) {
            event.setCancelled();
            return;
        }

        if (duel.getKit().getName().equals("tntsumo") && block.getId() == BlockID.TNT) {
            List<Entity> nearbyEntities = List.of(duel.getWorld().getEntities());
            for (Entity entity : nearbyEntities) {
                if (entity instanceof Player && ((Player) entity).getGamemode() == Player.SURVIVAL && entity.distance(block.getLocation()) <= 3) {
                    Vector3 direction = entity.getLocation().subtract(block.getLocation()).normalize();
                    Vector3 knockbackVector = new Vector3(
                            direction.x * 2.2,
                            direction.y * 1.5 + 0.5,
                            direction.z * 2.2
                    );
                    entity.setMotion(knockbackVector);
                }
            }

            event.setCancelled();
            return;
        } else {
            if (duel instanceof PartyDuel) {
                if (duel instanceof Bridge bridge) {
                    if (bridge.getPortal(party).isWithinPortalRadius(block.getLocation()) || bridge.getPortal(bridge.getOpponentParty(party)).isWithinPortalRadius(block.getLocation())) {
                        event.setCancelled();
                        return;
                    }
                } else if (duel instanceof BattleRush battleRush) {
                    battleRush.addBlock(block.getLocation());
                    return;
                }
            } else if (duel instanceof PartySplit) {
                if (duel instanceof alexis.practice.party.games.event.split.types.Bridge bridge) {
                    if (bridge.getPortal(profile).isWithinPortalRadius(block.getLocation()) || bridge.getOpponentPortal(profile).isWithinPortalRadius(block.getLocation())) {
                        event.setCancelled();
                        return;
                    }
                } else if (duel instanceof alexis.practice.party.games.event.split.types.BattleRush battleRush) {
                    battleRush.addBlock(block.getLocation());
                    return;
                }
            }
        }

        duel.addBlock(block);
    }

    @EventHandler
    public void handlePickup(InventoryPickupArrowEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory instanceof PlayerInventory)) return;

        InventoryHolder holder = inventory.getHolder();

        if (!(holder instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null || profile.getProfileData().getParty().getDuel() == null) return;

        if (profile.getProfileData().getParty().getDuel().getKit().getName().equals("bride")) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handleExplode(EntityExplodeEvent event) {
        Level level = event.getPosition().getLevel();
        Optional<PartyGame> optionalPartyGame =  PartyGamesManager.getInstance().getPartyGames().values().stream().filter(d -> d.getWorld().equals(level)).findFirst();

        if (optionalPartyGame.isEmpty()) return;

        PartyGame duel = optionalPartyGame.get();
        List<Block> allowedBlocks = new ArrayList<>();

        if (duel.getKit().getName().equals("fireball")) {
            List<Block> blocks = event.getBlockList();

            for (Block block : blocks) {
                if (block.getId() != BlockID.BED_BLOCK) {
                    if (duel instanceof PartyDuel) {
                        if (block.getId() == BlockID.END_STONE || block.getId() == 5) {
                            allowedBlocks.add(block);
                        } else if (duel.isBlock(block)) {
                            allowedBlocks.add(block);
                            duel.removeBlock(block);
                        }
                    } else if (duel instanceof PartySplit) {
                        if (block.getId() == BlockID.END_STONE || block.getId() == 5) {
                            allowedBlocks.add(block);
                        } else if (duel.isBlock(block)) {
                            allowedBlocks.add(block);
                            duel.removeBlock(block);
                        }
                    }
                }
            }
        }

        event.setBlockList(allowedBlocks);
    }

    @EventHandler
    public void handleProjectile(ProjectileHitEvent event) {
        Entity projectile = event.getEntity();
        Entity entity = ((EntityProjectile) projectile).shootingEntity;

        if (entity instanceof Player player && projectile instanceof EntityArrow) {
            Profile profile = ProfileManager.getInstance().get(player);

            if (profile == null || profile.getProfileData().getParty() == null || profile.getProfileData().getParty().getDuel() == null) {
                return;
            }

            PartyGame duel = profile.getProfileData().getParty().getDuel();

            if (duel instanceof PartyDuel) {
                if (duel instanceof Bridge bridge) {
                    bridge.addArrowCooldown(profile);
                }
            } else if (duel instanceof PartySplit) {
                if (duel instanceof alexis.practice.party.games.event.split.types.Bridge bridge) {
                    bridge.addArrowCooldown(profile);
                }
            }
        }
    }

    @EventHandler
    public void handleMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();

        if (duel.isSpectator(profile)) return;

        if (!duel.getCurrentState().equals(PartyGameState.RUNNING)) return;

        if (duel.getKit().getName().equals("sumo") || duel.getKit().getName().equals("tntsumo")) {
            Block block = player.getLevel().getBlock(player.getPosition());

            if (block instanceof BlockWater) {
                party.setDeath(profile);
            }
        } else if (duel instanceof PartyDuel) {
            if (duel instanceof Bridge bridge) {
                DuelWorld.Portal opponentPortal = bridge.getPortal(bridge.getOpponentParty(party));
                DuelWorld.Portal portal = bridge.getPortal(party);

                if (opponentPortal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                        bridge.addPoint(profile);
                        return;
                    }
                } else if (portal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                         bridge.setDeath(profile);
                         return;
                    }
                }

                int y = duel.getWorldData().getFirstPosition().getFloorY() - 25;

                if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                    bridge.setDeath(profile);
                }
            } else if (duel instanceof BattleRush battleRush) {
                DuelWorld.Portal opponentPortal = battleRush.getPortal(battleRush.getOpponentParty(party));
                DuelWorld.Portal portal = battleRush.getPortal(party);

                if (opponentPortal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                        battleRush.addPoint(profile);
                        return;
                    }
                } else if (portal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                        battleRush.setDeath(profile);
                        return;
                    }
                }

                int y = duel.getWorldData().getFirstPosition().getFloorY() - 20;

                if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                    battleRush.setDeath(profile);
                }
            } else if (duel instanceof alexis.practice.party.games.duel.types.BedFight bedFight) {
                int y = duel.getWorldData().getFirstPosition().getFloorY() - 15;

                if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                    bedFight.setDeath(profile);
                }
            }
        } else if (duel instanceof PartySplit) {
            if (duel instanceof alexis.practice.party.games.event.split.types.Bridge bridge) {
                DuelWorld.Portal opponentPortal = bridge.getOpponentPortal(profile);
                DuelWorld.Portal portal = bridge.getPortal(profile);

                if (opponentPortal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                        bridge.addPoint(profile);
                        return;
                    }
                } else if (portal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                        bridge.setDeath(profile);
                        return;
                    }
                }

                int y = duel.getWorldData().getFirstPosition().getFloorY() - 25;

                if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                    bridge.setDeath(profile);
                }
            } else if (duel instanceof alexis.practice.party.games.event.split.types.BattleRush battleRush) {
                DuelWorld.Portal opponentPortal = battleRush.getOpponentPortal(profile);
                DuelWorld.Portal portal = battleRush.getPortal(profile);

                if (opponentPortal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                        battleRush.addPoint(profile);
                        return;
                    }
                } else if (portal.contains(player.getPosition())) {
                    Block block = player.getLevel().getBlock(player.getPosition());

                    if (block.getId() == BlockID.END_PORTAL) {
                        battleRush.setDeath(profile);
                        return;
                    }
                }

                int y = duel.getWorldData().getFirstPosition().getFloorY() - 20;

                if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                    battleRush.setDeath(profile);
                }
            } else if (duel instanceof BedFight bedFight) {
                int y = duel.getWorldData().getFirstPosition().getFloorY() - 15;

                if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                    bedFight.setDeath(profile);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void handleDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (!duel.getCurrentState().equals(PartyGameState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled();
            return;
        }

        float finalHealth = player.getHealth() - event.getFinalDamage();

        if (finalHealth < 1 || (cause.equals(EntityDamageEvent.DamageCause.VOID))) {
            event.setCancelled(true);
            if (duel instanceof PartyDuel) {
                if (duel instanceof Bridge bridge) {
                    bridge.setDeath(profile);
                    return;
                } else if (duel instanceof alexis.practice.party.games.duel.types.BedFight bedFight) {
                    bedFight.setDeath(profile);
                    return;
                }
            } else if (duel instanceof PartySplit) {
                if (duel instanceof alexis.practice.party.games.event.split.types.Bridge bridge) {
                    bridge.setDeath(profile);
                    return;
                } else if (duel instanceof BedFight bedFight) {
                    bedFight.setDeath(profile);
                    return;
                }
            }

            party.setDeath(profile);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof Player damager)) {
                return;
            }

            Profile target = ProfileManager.getInstance().get(damager);

            if (target == null || target.getProfileData().getParty() == null || target.getProfileData().getParty().getDuel() == null) {
                return;
            }

            if (target.getProfileData().getParty().getDuel().getId() != duel.getId()) {
                return;
            }

            if (duel.getKit().getName().equals("tntsumo")) {
                int count = Utils.countItems(damager.getInventory().getContents().values(), BlockID.TNT);

                if (count <= 8) {
                    damager.getInventory().addItem(Block.get(BlockID.TNT).toItem());
                } else {
                    int countEnder = Utils.countItems(damager.getInventory().getContents().values(), ItemID.ENDER_PEARL);

                    if (countEnder == 0) {
                        damager.getInventory().addItem(Item.get(ItemID.ENDER_PEARL));
                    }
                }
            } else if (duel.getKit().isNeedScoreTag()) {
                player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", ((player.getHealth() + player.getAbsorption()) - event.getFinalDamage()))));
            }

            if (duel instanceof PartyDuel) {
                if (party.isMember(target)) {
                    event.setCancelled();
                    return;
                }

                if (duel instanceof Boxing boxing) {
                    boxing.setHit(party);
                }
            } else if (duel instanceof PartySplit partySplit) {
                if (partySplit.isSameTeam(profile, target)) {
                    event.setCancelled();
                    return;
                }

                if (duel instanceof alexis.practice.party.games.event.split.types.Boxing boxing) {
                    boxing.setHit(target);
                }
            }

            target.getCacheData().getCombat().set(profile);
            profile.getCacheData().getCombat().set(target);
        }
    }

    @EventHandler
    public void handleConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();

        if (duel.getKit().getName().equals("bridge") && event.getItem().getId() == ItemID.GOLDEN_APPLE) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @EventHandler
    public void handleRegainHealth(EntityRegainHealthEvent event) {
        int regainReason = event.getRegainReason();

        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();

        if (duel.getKit().isNeedScoreTag()) {
            if (regainReason == EntityRegainHealthEvent.CAUSE_EATING) {
                event.setCancelled();
                return;
            }

            player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", (player.getHealth() + player.getAbsorption()))));
        }
    }

    @EventHandler
    public void handleDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();

        if (!duel.getWorldData().isCanDrop()) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null) return;

        Party party = profile.getProfileData().getParty();

        if (party.getDuel() == null) return;

        PartyGame duel = party.getDuel();

        if (!duel.getCurrentState().equals(PartyGameState.RUNNING)) {
            event.setCancelled();
        }
    }
}
