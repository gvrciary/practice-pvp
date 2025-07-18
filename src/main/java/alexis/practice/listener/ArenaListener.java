package alexis.practice.listener;

import alexis.practice.arena.Arena;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.profile.settings.types.HiddenNonOpponents;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import cn.nukkit.event.player.PlayerBucketEmptyEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;

public class ArenaListener implements Listener {

    @EventHandler
    public void handleBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getArena() == null) return;

        Arena arena = profile.getProfileData().getArena();

        if (!arena.getArenaData().isCanBuild()) {
            event.setCancelled();
            return;
        }

        Block block = event.getBlock();

        if (block.getId() == Block.OBSIDIAN) return;

        if (arena.hasBlock(block.getLocation())) {
            arena.removeBlock(block.getLocation());
            event.setDrops(new Item[0]);
        } else {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handlePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getArena() == null) return;

        Arena arena = profile.getProfileData().getArena();

        if (!arena.getArenaData().isCanBuild()) {
            event.setCancelled();
            return;
        }

        Block block = event.getBlock();
        arena.addBlock(block.getLocation());
    }

    @EventHandler
    public void handleBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getArena() == null) return;

        Arena arena = profile.getProfileData().getArena();

        if (!arena.getArenaData().isCanBuild()) {
            event.setCancelled();
            return;
        }

        arena.addBlock(event.getBlockClicked().getLocation());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void handleDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (profile == null || profile.getProfileData().getArena() == null) return;

        Arena arena = profile.getProfileData().getArena();

        if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled();
            return;
        }

        float finalHealth = player.getHealth() - event.getFinalDamage();

        if (finalHealth < 1 || (cause.equals(EntityDamageEvent.DamageCause.VOID))) {
            event.setCancelled(true);
            arena.removePlayer(profile, false);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof Player damager)) return;

            Profile target = ProfileManager.getInstance().get(damager);

            if (target == null || target.getProfileData().getArena() == null) return;

            if (!target.getProfileData().getArena().getArenaData().getName().equals(arena.getArenaData().getName())) return;

            if (target.getIdentifier().equals(profile.getIdentifier())) return;

            Profile lastHitTarget = target.getCacheData().getCombat().get();
            Profile lastHitProfile = profile.getCacheData().getCombat().get();

            if (lastHitProfile != null) {
                if (!lastHitProfile.getIdentifier().equals(target.getIdentifier())) {
                    event.setCancelled();
                    return;
                }
            }

            if (lastHitTarget != null) {
                if (!lastHitTarget.getIdentifier().equals(profile.getIdentifier())) {
                    event.setCancelled();
                    return;
                }
            }

            if (arena.getKit().isNeedScoreTag()) {
                player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", ((player.getHealth() + player.getAbsorption()) - event.getFinalDamage()))));
            }

            target.getCacheData().getCombat().set(profile);
            profile.getCacheData().getCombat().set(target);

            if (profile.getSettingsData().isEnabled(SettingType.HIDDEN_NON_OPPONENTS.toString())) ((HiddenNonOpponents) SettingType.HIDDEN_NON_OPPONENTS.getSetting()).setHidden(profile, true);
            if (target.getSettingsData().isEnabled(SettingType.HIDDEN_NON_OPPONENTS.toString())) ((HiddenNonOpponents) SettingType.HIDDEN_NON_OPPONENTS.getSetting()).setHidden(target, true);
        }
    }

    @EventHandler
    public void handleRegainHealth(EntityRegainHealthEvent event) {
        int regainReason = event.getRegainReason();

        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getArena() == null) return;

        Arena arena = profile.getProfileData().getArena();

        if (arena.getKit().isNeedScoreTag()) {
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

        if (profile == null || profile.getProfileData().getArena() == null) return;

        event.setCancelled();
    }
}
