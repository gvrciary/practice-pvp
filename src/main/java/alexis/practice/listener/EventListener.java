package alexis.practice.listener;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.games.EventState;
import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.border.Border;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import alexis.practice.event.games.types.meetup.scenarios.ScenarioManager;
import alexis.practice.event.games.types.meetup.scenarios.defaults.DoNotDisturb;
import alexis.practice.event.games.types.meetup.scenarios.defaults.NoClean;
import alexis.practice.event.games.types.meetup.scenarios.defaults.SafeLoot;
import alexis.practice.event.games.types.skywars.Skywars;
import alexis.practice.event.games.types.sumo.Sumo;
import alexis.practice.event.games.types.tournament.Tournament;
import alexis.practice.event.games.types.tournament.match.Match;
import alexis.practice.event.team.Team;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockTransparentMeta;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;

import java.util.List;

public class EventListener implements Listener {

    @EventHandler
    public void handleBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null) return;

        Block block = event.getBlock();
        Event events = profile.getProfileData().getEvent();

        if (events.getEventArena() == null) return;

        EventArena eventArena = events.getEventArena();

        if (block.getId() == Block.OBSIDIAN) return;

        if (!eventArena.getCurrentState().equals(EventState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (eventArena instanceof Tournament tournament) {
            Match match = tournament.inAnyMatch(profile);

            if (match != null) {
                if (!match.isBlock(block)) {
                    event.setCancelled();
                    return;
                }

                match.removeBlock(block);
                return;
            }
        }

        if (eventArena instanceof Skywars skywars) {
            if (block.getId() == BlockID.CHEST || block.getId() == BlockID.TRAPPED_CHEST) {
                skywars.getChestManager().removeChest(block.getLocation());
            } else if (block.getId() == BlockID.GLOWING_REDSTONE_ORE) {
                Effect effect = player.getEffect(Effect.ABSORPTION);
                if (effect != null && effect.getAmplifier() < 5) {
                    player.addEffect(Effect.getEffect(Effect.ABSORPTION).setAmplifier(effect.getAmplifier() + 1).setVisible(false));
                } else {
                    player.addEffect(Effect.getEffect(Effect.ABSORPTION).setAmplifier(0).setVisible(false));
                }
            }

            return;
        }

        if (!eventArena.isBlock(block)) {
            event.setCancelled();
            return;
        }

        eventArena.removeBlock(block);
    }

    @EventHandler
    public void handlePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null) return;

        Block block = event.getBlock();
        Event events = profile.getProfileData().getEvent();

        if (events.getEventArena() == null) return;

        EventArena eventArena = events.getEventArena();

        if (!eventArena.getCurrentState().equals(EventState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (eventArena instanceof Tournament tournament) {
            Match match = tournament.inAnyMatch(profile);

            if (match != null) {
                if (match.isLimitY(block.getLocation().getFloorY())) {
                    event.setCancelled();
                    return;
                }

                if (eventArena.getEvent().getKit().getName().equals("tntsumo") && block.getId() == BlockID.TNT) {
                    List<Entity> nearbyEntities = List.of(tournament.getWorld().getEntities());
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

                match.addBlock(block);
                return;
            }
        } else if (eventArena instanceof Skywars && (block.getId() == BlockID.CHEST || block.getId() == BlockID.TRAPPED_CHEST)) {
            event.setCancelled();
            return;
        } else if (eventArena instanceof Sumo sumo && sumo.getEvent().getKit().getName().equals("tntsumo") && block.getId() == BlockID.TNT && sumo.inFight(profile)) {
            List<Entity> nearbyEntities = List.of(sumo.getWorld().getEntities());
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

        eventArena.addBlock(block);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void handleDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null) return;

        Event events = profile.getProfileData().getEvent();

        if (events.getEventArena() == null) return;

        EventArena eventArena = events.getEventArena();
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (!eventArena.getCurrentState().equals(EventState.RUNNING)) {
            event.setCancelled();
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled();
            return;
        }

        if (eventArena instanceof Meetup meetup) {
            Scenario noclean = meetup.getScenarioManager().get(ScenarioManager.NOCLEAN);

            if (noclean.isEnabled() && noclean instanceof NoClean nC && nC.isInNoClean(profile)) {
                if (nC.isValid(profile)) {
                    event.setCancelled();
                    return;
                } else {
                    nC.remove(profile);
                }
            }

            Scenario fireless = meetup.getScenarioManager().get(ScenarioManager.FIRELESS);

            if (fireless.isEnabled()) {
                if (cause.equals(EntityDamageEvent.DamageCause.FIRE) || cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.LAVA)) {
                    event.setCancelled();
                }
            }
        }

        float finalHealth = player.getHealth() - event.getFinalDamage();

        if (finalHealth < 1 || (cause.equals(EntityDamageEvent.DamageCause.VOID))) {
            event.setCancelled(true);
            events.setDeath(profile);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof Player damager)) {
                return;
            }

            Profile target = ProfileManager.getInstance().get(damager);

            if (target == null || target.getProfileData().getEvent() == null || target.getProfileData().getEvent().getEventArena() == null) return;

            if (target.getProfileData().getEvent().getId() != events.getId()) return;

            if (events.isTeam()) {
                Team teamProfile = events.getTeamManager().getTeam(profile);
                Team teamTarget = events.getTeamManager().getTeam(target);

                if (teamProfile != null && teamTarget != null) {
                    if (teamProfile.getId() == teamTarget.getId()) {
                        event.setCancelled();
                        return;
                    }
                }
            }

            if (eventArena instanceof Meetup meetup) {
                Scenario noclean = meetup.getScenarioManager().get(ScenarioManager.NOCLEAN);

                if (noclean.isEnabled() && noclean instanceof NoClean nC) {
                    if (nC.isInNoClean(profile)) {
                        nC.remove(profile);

                        if (nC.isValid(profile)) {
                            damager.sendMessage(TextFormat.colorize("&cYou have lost your invulnerability"));
                        }
                    }
                }

                Scenario dnd = meetup.getScenarioManager().get(ScenarioManager.DONOTDISTURB);

                if (dnd.isEnabled() && dnd instanceof DoNotDisturb doNotDisturb) {
                    if (events.isTeam()) {
                        Team teamProfile = events.getTeamManager().getTeam(profile);
                        Team teamTarget = events.getTeamManager().getTeam(target);

                        DoNotDisturb.Data dataProfile = doNotDisturb.getData(profile);

                        if (dataProfile != null && teamTarget != null) {
                            if (dataProfile.getTime() > System.currentTimeMillis() && !dataProfile.getEnemy().equals(String.valueOf(teamTarget.getId()))) {
                                event.setCancelled();
                                return;
                            }
                        }

                        DoNotDisturb.Data dataTarget = doNotDisturb.getData(target);

                        if (dataTarget != null && teamProfile != null) {
                            if (dataTarget.getTime() > System.currentTimeMillis() && !dataTarget.getEnemy().equals(String.valueOf(teamProfile.getId()))) {
                                event.setCancelled();
                                return;
                            }
                        }

                        if (teamProfile != null && teamTarget != null) {
                            doNotDisturb.setDND(profile, target);
                        }
                    } else {
                        DoNotDisturb.Data dataProfile = doNotDisturb.getData(profile);

                        if (dataProfile != null) {
                            if (dataProfile.getTime() > System.currentTimeMillis() && !dataProfile.getEnemy().equals(target.getIdentifier())) {
                                event.setCancelled();
                                return;
                            }
                        }

                        DoNotDisturb.Data dataTarget = doNotDisturb.getData(target);

                        if (dataTarget != null) {
                            if (dataTarget.getTime() > System.currentTimeMillis() && !dataTarget.getEnemy().equals(profile.getIdentifier())) {
                                event.setCancelled();
                                return;
                            }
                        }

                        doNotDisturb.setDND(profile, target);
                    }
                }
            } else if (eventArena instanceof Sumo sumo) {
                if (!sumo.inFight(profile) || !sumo.inFight(target)) {
                    event.setCancelled();
                    return;
                }

                if (sumo.getEvent().getKit().getName().equals("tntsumo")) {
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
            } else if (eventArena instanceof Tournament tournament) {
                Match matchProfile = tournament.inAnyMatch(profile);
                Match matchTarget = tournament.inAnyMatch(target);

                if ((matchProfile == null || matchTarget == null) || (!matchTarget.inThisMatch(profile) || !matchProfile.inThisMatch(target))) {
                    event.setCancelled();
                    return;
                }
            }

            if (events.getKit().isNeedScoreTag()) {
                player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", ((player.getHealth() + player.getAbsorption()) - event.getFinalDamage()))));
            }

            target.getCacheData().getCombat().set(profile);
            profile.getCacheData().getCombat().set(target);
        }
    }

    @EventHandler
    public void handleRegainHealth(EntityRegainHealthEvent event) {
        int regainReason = event.getRegainReason();

        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null) return;

        Event events = profile.getProfileData().getEvent();

        if (events.getEventArena() == null) return;

        if (events.getKit().isNeedScoreTag()) {
            if (regainReason == EntityRegainHealthEvent.CAUSE_EATING) {
                event.setCancelled();
                return;
            }

            player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", (player.getHealth() + player.getAbsorption()))));
        }
    }

    @EventHandler
    public void handleMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null) return;

        Event events = profile.getProfileData().getEvent();

        if (events.getEventArena() == null) return;

        EventArena eventArena = events.getEventArena();

        if (eventArena.isSpectator(profile)) return;

        if (!eventArena.getCurrentState().equals(EventState.RUNNING)) return;

        if (eventArena instanceof Sumo sumo && sumo.inFight(profile)) {
            Block block = player.getLevel().getBlock(player.getPosition());

            if (block instanceof BlockWater) {
                events.setDeath(profile);
            }
        } else if (eventArena instanceof Meetup meetup) {
            Border border = meetup.getBorder();

            if (border.getSize() == 25 && player.getY() < 98) {
                border.teleportInside(player, true);
                return;
            }

            if (!border.insideBorder(player)) {
                border.teleportInside(player, true);
            }
        }
    }

    @EventHandler
    public void handleDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null) return;

        Event events = profile.getProfileData().getEvent();

        if (events.getEventArena() == null) return;

        EventArena eventArena = events.getEventArena();

        if (!eventArena.isCanDropItem()) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);
        Block block = event.getBlock();

        if (profile == null || profile.getProfileData().getEvent() == null) return;

        Event events = profile.getProfileData().getEvent();

        if (events.getEventArena() == null) return;

        EventArena eventArena = events.getEventArena();

        if (!eventArena.getCurrentState().equals(EventState.RUNNING)) {
            event.setCancelled();
            return;
        }

        Item item = event.getItem();

        if (block != null && (block.getId() == BlockID.CHEST || block.getId() == BlockID.TRAPPED_CHEST) && eventArena instanceof Skywars skywars) {
            skywars.getChestManager().setChest(block.getLocation(), (BlockTransparentMeta) block);
            return;
        }

        if (eventArena instanceof Meetup meetup) {
            Scenario timebomb = meetup.getScenarioManager().get(ScenarioManager.TIMEBOMB);
            Scenario safeLoot = meetup.getScenarioManager().get(ScenarioManager.SAFELOOT);

            if (block != null && block.getId() == BlockID.CHEST && safeLoot.isEnabled() && timebomb.isEnabled() && safeLoot instanceof SafeLoot sL) {
                if (!sL.isOwner(profile, block.getLocation())) {
                    player.sendMessage(TextFormat.colorize("&cThis chest is not yours"));
                    event.setCancelled();
                }
                return;
            }

            Scenario rodless = meetup.getScenarioManager().get(ScenarioManager.RODLESS);

            if (rodless.isEnabled() && item != null && item.getId() == Item.FISHING_ROD) {
                event.setCancelled();
                return;
            }

            Scenario bowless = meetup.getScenarioManager().get(ScenarioManager.BOWLESS);

            if (bowless.isEnabled() && item != null && item.getId() == Item.BOW) {
                event.setCancelled();
            }
        }
    }

}
