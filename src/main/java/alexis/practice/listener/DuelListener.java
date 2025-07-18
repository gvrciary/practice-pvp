package alexis.practice.listener;

import alexis.practice.duel.Duel;
import alexis.practice.duel.Duel2vs2;
import alexis.practice.duel.DuelManager;
import alexis.practice.duel.DuelState;
import alexis.practice.duel.types.BattleRush;
import alexis.practice.duel.types.BedFight;
import alexis.practice.duel.types.Boxing;
import alexis.practice.duel.types.Bridge;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
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

public final class DuelListener implements Listener {

    @EventHandler
    public void handleBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        Block block = event.getBlock();
        Duel duel = profile.getProfileData().getDuel();

        if (!duel.getState().equals(DuelState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (block.getId() == Block.OBSIDIAN) return;

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
        }

        if (!duel.isBlock(block)) {
            event.setCancelled();
            return;
        }

        duel.removeBlock(block);

        if (duel instanceof BattleRush) {
            event.setDrops(new Item[0]);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handlePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        Block block = event.getBlock();
        Duel duel = profile.getProfileData().getDuel();

        if (!duel.getState().equals(DuelState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (duel.isLimitY(block.getLocation().getFloorY())) {
            event.setCancelled();
            return;
        }

        if (duel instanceof Bridge bridge) {
            if (bridge.getPortal(duel.getOpponentProfile(profile)).isWithinPortalRadius(block.getLocation()) || bridge.getPortal(profile).isWithinPortalRadius(block.getLocation())) {
                event.setCancelled();
                return;
            }
        } else if (duel.getKit().getName().equals("tntsumo") && block.getId() == BlockID.TNT) {
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

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        if (profile.getProfileData().getDuel().getKit().getName().equals("bride")) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handleExplode(EntityExplodeEvent event) {
        Level level = event.getPosition().getLevel();
        Optional<Duel> optionalDuel =  DuelManager.getInstance().getDuels().values().stream().filter(d -> d.getWorld().equals(level)).findFirst();

        if (optionalDuel.isEmpty()) return;

        Duel duel = optionalDuel.get();
        List<Block> allowedBlocks = new ArrayList<>();

        if (duel.getKit().getName().equals("fireball")) {
            List<Block> blocks = event.getBlockList();

            for (Block block : blocks) {
                if (block.getId() != BlockID.BED_BLOCK) {
                    if (block.getId() == BlockID.END_STONE || block.getId() == 5) {
                        allowedBlocks.add(block);
                    } else if (duel.isBlock(block)) {
                        allowedBlocks.add(block);
                        duel.removeBlock(block);
                    }
                }
            }
        }

        event.setBlockList(allowedBlocks);
    }

    @EventHandler
    public void handleConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        Duel duel = profile.getProfileData().getDuel();

        if (duel instanceof Bridge && event.getItem().getId() == ItemID.GOLDEN_APPLE) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @EventHandler
    public void handleMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        Duel duel = profile.getProfileData().getDuel();

        if (duel.isSpectator(profile)) return;

        if (!duel.getState().equals(DuelState.RUNNING)) return;

        if (duel instanceof BedFight) {
            int y = duel.getWorldData().getFirstPosition().getFloorY() - 15;

            if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                duel.setDeath(profile);
            }
        } else if (duel instanceof Bridge bridge) {
            DuelWorld.Portal opponentPortal = bridge.getPortal(duel.getOpponentProfile(profile));
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
                    duel.setDeath(profile);
                    return;
                }
            }

            int y = duel.getWorldData().getFirstPosition().getFloorY() - 25;

            if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                duel.setDeath(profile);
            }
        }  else if (duel instanceof BattleRush battleRush) {
            DuelWorld.Portal opponentPortal = battleRush.getPortal(duel.getOpponentProfile(profile));
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
                    duel.setDeath(profile);
                    return;
                }
            }

            int y = duel.getWorldData().getFirstPosition().getFloorY() - 20;

            if (player.getY() <= y && player.getLevel().getBlock(player.getPosition()).getId() == BlockID.AIR && player.getGamemode() == Player.SURVIVAL) {
                duel.setDeath(profile);
            }
        } else if (duel.getKit().getName().equals("sumo") || duel.getKit().getName().equals("tntsumo")) {
            Block block = player.getLevel().getBlock(player.getPosition());

            if (block instanceof BlockWater) {
                duel.setDeath(profile);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void handleDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        Duel duel = profile.getProfileData().getDuel();

        if (!duel.getState().equals(DuelState.RUNNING)) {
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
            duel.setDeath(profile);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof Player damager)) {
                return;
            }

            Profile target = ProfileManager.getInstance().get(damager);

            if (target == null || target.getProfileData().getDuel() == null || target.getProfileData().getDuel().getId() != duel.getId()) {
                return;
            }

            duel.getDuelStatistic().setHit(target, event.getFinalDamage());

            if (duel instanceof Boxing boxing) {
                boxing.check(target);
            } else if (duel instanceof Bridge || duel instanceof BedFight || duel instanceof BattleRush) {
                profile.getCacheData().getCombat().set(target);
                target.getCacheData().getCombat().set(profile);
            } else if (duel.getKit().isNeedScoreTag()) {
                player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", ((player.getHealth() + player.getAbsorption()) - event.getFinalDamage()))));
            } else if (duel.getKit().getName().equals("tntsumo")) {
                int count = Utils.countItems(damager.getInventory().getContents().values(), BlockID.TNT);

                if (count <= 8) {
                    damager.getInventory().addItem(Block.get(BlockID.TNT).toItem());
                } else {
                    int countEnder = Utils.countItems(damager.getInventory().getContents().values(), ItemID.ENDER_PEARL);

                    if (countEnder == 0) {
                        damager.getInventory().addItem(Item.get(ItemID.ENDER_PEARL));
                    }
                }
            }

            if (duel instanceof Duel2vs2) {
                target.getCacheData().getCombat().set(profile);
                profile.getCacheData().getCombat().set(target);
            }
        }
    }

    @EventHandler
    public void handleRegainHealth(EntityRegainHealthEvent event) {
        int regainReason = event.getRegainReason();

        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        Duel duel = profile.getProfileData().getDuel();

        if (duel.getKit().isNeedScoreTag()) {
            if (regainReason == EntityRegainHealthEvent.CAUSE_EATING) {
                event.setCancelled();
                return;
            }

            player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", (player.getHealth() + player.getAbsorption()))));
        }
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        Duel duel = profile.getProfileData().getDuel();

        if (!duel.getState().equals(DuelState.RUNNING)) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handleDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getDuel() == null) return;

        if (!profile.getProfileData().getDuel().isCanDropItem()) {
            event.setCancelled();
        }
    }

}
