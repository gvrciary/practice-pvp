package alexis.practice.listener;

import alexis.practice.Practice;
import alexis.practice.arena.world.ArenaWorldSetup;
import alexis.practice.duel.DuelManager;
import alexis.practice.duel.types.Bridge;
import alexis.practice.duel.world.DuelWorldSetup;
import alexis.practice.entity.ArrowEntity;
import alexis.practice.entity.EnderPearlEntity;
import alexis.practice.entity.FireballEntity;
import alexis.practice.entity.TntEntity;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.games.EventType;
import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.border.Border;
import alexis.practice.event.games.types.tournament.Tournament;
import alexis.practice.event.games.types.tournament.match.Match;
import alexis.practice.event.world.EventWorldSetup;
import alexis.practice.item.DefaultItem;
import alexis.practice.item.HotbarItem;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.item.object.ItemDefault;
import alexis.practice.kit.setup.KitEditor;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGame;
import alexis.practice.party.games.duel.PartyDuel;
import alexis.practice.party.games.event.split.PartySplit;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.data.ProfileData;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.profile.settings.types.CPSCounter;
import alexis.practice.profile.settings.types.GameTime;
import alexis.practice.profile.settings.types.MoreCritical;
import alexis.practice.util.InputMode;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import alexis.practice.util.handler.StaffHandler;
import alexis.practice.util.server.ServerEssential;
import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.LeavesDecayEvent;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.event.server.QueryRegenerateEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import cn.nukkit.item.ItemArrow;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.*;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.DyeColor;
import cn.nukkit.utils.TextFormat;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProfileListener implements Listener {

    private static final Set<String> freezes = new HashSet<>();

    @EventHandler
    public void handleBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().isInSetupMode() || profile.getProfileData().inStaffMode() || profile.getProfileData().getQueue() != null) {
            event.setCancelled();
            return;
        }

        if (profile.getProfileData().isInLobby()) {
            if (!player.hasPermission("build.permission") || player.getGamemode() != Player.CREATIVE) {
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void handlePickupItem(InventoryPickupItemEvent event) {
        if (!(event.getInventory() instanceof PlayerInventory inventory)) return;
        if (!(inventory.getHolder() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (profile.getProfileData().inStaffMode() || profile.getProfileData().isInLobby() || profile.getProfileData().isInSetupMode() ||
        (profile.getProfileData().getParty() != null && profile.getProfileData().getParty().isInLobby()) || profile.getProfileData().getQueue() != null) {
            event.setCancelled();
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void handlePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().isInSetupMode() || profile.getProfileData().inStaffMode() || profile.getProfileData().getQueue() != null) {
            event.setCancelled();
            return;
        }

        if (profile.getProfileData().isInLobby()) {
            if (!player.hasPermission("build.permission") || player.getGamemode() != Player.CREATIVE) {
                event.setCancelled();
                return;
            }
        }

        if (!event.isCancelled() && !profile.getProfileData().isInSetupMode()) {
            Block block = event.getBlock();

            if (block.getId() == BlockID.TNT) {
                DefaultItem.TNT.getItemInstance().executeUse(profile, block.toItem());
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void handleCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        String message = event.getMessage();

        if (message.startsWith("/tell") || message.startsWith("/msg")) {
            String[] args = message.split(" ");
            String[] tell = Arrays.stream(args, 2, args.length).toArray(String[]::new);

            Player targetPlayer = player.getServer().getPlayer(args[1]);

            if (targetPlayer == null) {
                return;
            }

            Profile target = ProfileManager.getInstance().get(targetPlayer);

            if (target == null) {
                return;
            }

            if (profile.getSettingsData().isEnabled(SettingType.NO_PRIVATE_MESSAGES.toString())) {
                event.setCancelled();
                player.sendMessage(TextFormat.colorize("&cYou cannot send messages to this player because he does not receive private messages"));
            } else if (tell.length > 0) {
                if (target.getSettingsData().isEnabled(SettingType.PRIVATE_MESSAGE_SOUND.toString())) {
                    PlayerUtil.playSound(targetPlayer, "random.orb");
                }

                StaffHandler.getInstance().sendMessage("&r&7" + profile.getName() + " to " + target.getName() + ":&f " + String.join(" ", tell));
                target.getCacheData().setLastMessage(profile);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleSleep(PlayerBedEnterEvent event) {
        event.setCancelled();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleMissedSwing(PlayerMissedSwingEvent event) {
        event.setCancelled();
    }

    @EventHandler
    public void handleEntityArmor(EntityArmorChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (!(event.getNewItem() instanceof ItemArmor item)) return;

        if (profile.getProfileData().isInSetupMode()) {
            KitEditor.Setup setup = KitEditor.getInstance().get(profile);
            if (setup != null) {
                event.setCancelled();

                player.getInventory().addItem(item);
            }
        }
    }

    @EventHandler
    public void handleTransaction(InventoryTransactionEvent event) {
        Profile profile = ProfileManager.getInstance().get(event.getTransaction().getSource());

        if (profile == null) return;

        event.getTransaction().getActions().forEach(action -> {
            Item item = action.getSourceItem();

            if (action instanceof SlotChangeAction) {
                if (item.getNamedTag() != null && (item.getNamedTag().exist("custom_item") || item.getNamedTag().exist("staff_item"))) {
                    event.setCancelled();
                }
            }
        });
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getDamager() instanceof Player damager)) return;

        Profile profile = ProfileManager.getInstance().get(player);
        Profile target = ProfileManager.getInstance().get(damager);

        if (profile == null || target == null) return;

        if (freezes.contains(profile.getIdentifier()) && !target.getProfileData().inStaffMode()) {
            event.setCancelled();
            return;
        }

        if (target.getProfileData().inStaffMode()) {
            Item item = damager.getInventory().getItemInHand();

            if (item != null && item.getNamedTag() != null && item.getNamedTag().exist("staff_item")) {
                event.setCancelled();

                if (item.equals(Block.get(Block.ICE).toItem(), false, false)) {
                    if (!freezes.contains(profile.getIdentifier())) {
                        player.addEffect(Effect.getEffect(Effect.BLINDNESS).setAmplifier(1).setDuration(Integer.MAX_VALUE));
                        freezes.add(profile.getIdentifier());
                        player.setImmobile();

                        player.sendMessage(TextFormat.colorize("&cYou are frozen."));
                        damager.sendMessage(TextFormat.colorize("&cYou have frozen " + player.getName()));
                    } else {
                        player.removeEffect(Effect.BLINDNESS);
                        freezes.remove(profile.getIdentifier());
                        player.setImmobile(false);

                        player.sendMessage(TextFormat.colorize("&aYou are unfrozen."));
                        damager.sendMessage(TextFormat.colorize("&aYou have unfrozen " + player.getName()));
                    }
                } else if (item.equals(Item.get(ItemID.BOOK), false, false)) {
                    damager.sendMessage(TextFormat.colorize("&6----INFORMATION----\n&7Name: &f" + profile.getName() + "\n&7Device OS: &f" + cn.nukkit.utils.Utils.getOS(player)
                            + "\n&7Input Mode:&f " + InputMode.fromOrdinal(player.getLoginChainData().getCurrentInputMode()).getEnumName() + "\n&7Version:&f " +  cn.nukkit.utils.Utils.getVersionByProtocol(player.protocol)));
                }
            }
            return;
        }

        if (profile.getProfileData().isInLobby() && target.getProfileData().isInLobby() && profile.getProfileData().getQueue() == null && target.getProfileData().getQueue() == null) {
            String item = TextFormat.clean(damager.getInventory().getItemInHand().getCustomName());

            if (item.equals(TextFormat.clean(HotbarItem.DUELS.getItem().getCustomName()))) {
                if (profile.getSettingsData().isEnabled(SettingType.NO_DUEL_INVITATIONS.toString())) {
                    damager.sendMessage(TextFormat.colorize("&cThis player does not accept invitations."));
                    return;
                }

                if (profile.getProfileData().getQueue() == null && profile.getProfileData().isInLobby()) {
                    DuelManager.getInstance().sendDuelForm(target, profile);
                } else {
                    damager.sendMessage(TextFormat.colorize("&cYou can't send duel to " + profile.getName()));
                }
            }

            return;
        }

        if (!profile.getProfileData().isInLobby()) {
            MoreCritical more = (MoreCritical) SettingType.MORE_CRITICAL.getSetting();

            if (profile.getSettingsData().isEnabled(SettingType.MORE_CRITICAL.toString())) {
                more.add(profile, player);
            }
        }
    }

    @EventHandler
    public void handleCombust(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().inStaffMode() || profile.getProfileData().isInLobby()) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handleConsume(PlayerItemConsumeEvent event) {
        Profile profile = ProfileManager.getInstance().get(event.getPlayer());

        if (profile == null || profile.getProfileData().inStaffMode() || profile.getProfileData().isInSetupMode()) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getEntity();

        if (event.getEntity().getName().equals("Hologram")) event.setCancelled();

        if (!(entity instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (profile == null) return;

        if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled();
            return;
        }

        if (profile.getProfileData().isInLobby() || profile.getProfileData().isInSetupMode() || profile.getProfileData().inStaffMode()) {
            event.setCancelled();

            if (cause.equals(EntityDamageEvent.DamageCause.VOID)) {
                player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());
            }

            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party != null && party.isInLobby()) {
            event.setCancelled();

            if (cause.equals(EntityDamageEvent.DamageCause.VOID)) {
                player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());
            }

            return;
        }

        if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            if (event.isCancelled()) return;

            Entity damager = entityDamageByEntityEvent.getDamager();

            int attackCooldown = 8;
            ProfileData profileData = profile.getProfileData();

            if (profileData.getDuel() != null) {
                attackCooldown = profileData.getDuel().getKit().getAttackCooldown();
            } else if (profileData.getArena() != null) {
                attackCooldown = profileData.getArena().getKit().getAttackCooldown();
            } else if (profileData.getParty() != null && profileData.getParty().getDuel() != null) {
                attackCooldown = profileData.getParty().getDuel().getKit().getAttackCooldown();
            } else if (profileData.getEvent() != null && profileData.getEvent().getKit() != null) {
                attackCooldown = profileData.getEvent().getKit().getAttackCooldown();
            }

            entityDamageByEntityEvent.setKnockBack(0);
            event.setAttackCooldown(attackCooldown);

            profile.knockback(damager);
        }
    }

    @EventHandler
    public void handleDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        Item item = event.getItem();

        if (item != null && item.getNamedTag() != null && (item.getNamedTag().exist("custom_item") || item.getNamedTag().exist("staff_item"))) {
            event.setCancelled();
            return;
        }

        if (profile.getProfileData().isInSetupMode()) {
            KitEditor.Setup kitEditorSetup = KitEditor.getInstance().get(profile);

            if (kitEditorSetup != null) {
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void handleMotion(EntityMotionEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (profile.isInitialKnockbackMotion()) {
            profile.setInitialKnockbackMotion(false);
            profile.setCancelKnockbackMotion(true);
        } else if (profile.isCancelKnockbackMotion()) {
            profile.setCancelKnockbackMotion(false);
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled();
    }

    @EventHandler
    public void handleChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (profile.getProfileData().isInSetupMode()) {
            DuelWorldSetup.Setup duelWorldSetup = DuelWorldSetup.getInstance().get(profile);
            ArenaWorldSetup.Setup arenaWorldSetup = ArenaWorldSetup.getInstance().get(profile);
            EventWorldSetup.Setup eventWorldSetup = EventWorldSetup.getInstance().get(profile);
            KitEditor.Setup kitEditorSetup = KitEditor.getInstance().get(profile);

            if (duelWorldSetup != null) {
                String[] args = message.split(" ");

                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "cancel" -> {
                        duelWorldSetup.destroy();
                        event.setCancelled();
                        return;
                    }
                    case "save" -> {
                        duelWorldSetup.save();
                        event.setCancelled();
                        return;
                    }
                    case "info" -> {
                        final StringBuilder duelInfo = new StringBuilder();

                        duelInfo.append("&3Duel World Setup Information").append("\n");
                        if (duelWorldSetup.getWorld() != null) {
                            duelInfo.append("&fDuel World Name: &3").append(duelWorldSetup.getWorld().getFolderName()).append("\n");
                        } else {
                            duelInfo.append("&fDuel World Name: &3Not defined\n");
                        }
                        duelInfo.append("&fDuel World Duel Type: &3").append(duelWorldSetup.getType()).append("\n");
                        duelInfo.append("&fDuel World Need Portal: &3").append(duelWorldSetup.isNeedPortal()).append("\n");
                        duelInfo.append("&fDuel World First Position: &3");
                        if (duelWorldSetup.getFirstPosition() != null) {
                            duelInfo.append(duelWorldSetup.getFirstPosition().getFloorX()).append(", ").append(duelWorldSetup.getFirstPosition().getFloorY()).append(", ").append(duelWorldSetup.getFirstPosition().getFloorZ());
                        } else {
                            duelInfo.append("Not defined");
                        }
                        duelInfo.append("\n");
                        duelInfo.append("&fDuel World Second Position: &3");
                        if (duelWorldSetup.getSecondPosition() != null) {
                            duelInfo.append(duelWorldSetup.getSecondPosition().getFloorX()).append(", ").append(duelWorldSetup.getSecondPosition().getFloorY()).append(", ").append(duelWorldSetup.getSecondPosition().getFloorZ());
                        } else {
                            duelInfo.append("Not defined");
                        }

                        if (duelWorldSetup.isNeedPortal()) {
                            duelInfo.append("\n").append("&fDuel World First Portal: &3");
                            if (duelWorldSetup.getFirstPortal() != null) {
                                duelInfo.append(duelWorldSetup.getFirstPortal().getFloorX()).append(", ").append(duelWorldSetup.getFirstPortal().getFloorY()).append(", ").append(duelWorldSetup.getFirstPortal().getFloorZ());
                            } else {
                                duelInfo.append("Not defined");
                            }
                            duelInfo.append("\n").append("&fDuel World Second Portal: &3");
                            if (duelWorldSetup.getSecondPortal() != null) {
                                duelInfo.append(duelWorldSetup.getSecondPortal().getFloorX()).append(", ").append(duelWorldSetup.getSecondPortal().getFloorY()).append(", ").append(duelWorldSetup.getSecondPortal().getFloorZ());
                            } else {
                                duelInfo.append("Not defined");
                            }
                        }

                        player.sendMessage(TextFormat.colorize(duelInfo.toString()));
                        event.setCancelled();
                        return;
                    }
                }
            } else if (arenaWorldSetup != null) {
                String[] args = message.split(" ");

                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "cancel" -> {
                        arenaWorldSetup.destroy();
                        event.setCancelled();
                        return;
                    }
                    case "save" -> {
                        arenaWorldSetup.save();
                        event.setCancelled();
                        return;
                    }
                    case "remove-spawns" -> {
                        arenaWorldSetup.removeArenaSpawns();
                        event.setCancelled();
                        return;
                    }
                    case "info" -> {
                        final StringBuilder arenaInfo = new StringBuilder("&5Arena Setup Info\n");

                        arenaInfo.append("&fArena Name: &6").append(arenaWorldSetup.getArenaName()).append("\n");
                        if (arenaWorldSetup.getArenaWorld() != null) {
                            arenaInfo.append("&fArena World: &6").append(arenaWorldSetup.getArenaWorld().getFolderName()).append("\n");
                        } else {
                            arenaInfo.append("&fArena World: &5Not defined\n");
                        }
                        if (arenaWorldSetup.getArenaKit() != null) {
                            arenaInfo.append("&fArena Kit: &6").append(arenaWorldSetup.getArenaKit()).append("\n");
                        } else {
                            arenaInfo.append("&fArena Kit: &5Not defined\n");
                        }
                        arenaInfo.append("&fArena Spawns: &f");

                        List<Vector3> arenaSpawns = arenaWorldSetup.getArenaSpawns();
                        if (arenaSpawns.isEmpty()) {
                            arenaInfo.append("None");
                        } else {
                            for (int i = 0; i < arenaSpawns.size(); i++) {
                                Vector3 spawn = arenaSpawns.get(i);
                                arenaInfo.append(spawn.getFloorX()).append(":").append(spawn.getFloorY()).append(":").append(spawn.getFloorZ());
                                if (i < arenaSpawns.size() - 1) {
                                    arenaInfo.append(", ");
                                }
                            }
                        }

                        player.sendMessage(TextFormat.colorize(arenaInfo.toString()));
                        event.setCancelled();
                        return;
                    }
                }
            } else if (eventWorldSetup != null) {
                String[] args = message.split(" ");

                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "cancel" -> {
                        eventWorldSetup.destroy();
                        event.setCancelled();
                        return;
                    }
                    case "save" -> {
                        eventWorldSetup.save();
                        event.setCancelled();
                        return;
                    }
                    case "remove-spawns" -> {
                        eventWorldSetup.removeArenaSpawns();
                        event.setCancelled();
                        return;
                    }
                    case "info" -> {
                        final StringBuilder eventInfo = new StringBuilder("&5Arena Setup Info\n");

                        eventInfo.append("&fEvent Type: &6").append(eventWorldSetup.getType().getName()).append("\n");
                        eventInfo.append("&fEvent World: &6").append(eventWorldSetup.getWorld().getFolderName()).append("\n");

                        if (eventWorldSetup.getType().equals(EventType.SKYWARS)) {
                            eventInfo.append("&fEvent Spawns: &f");

                            List<Vector3> arenaSpawns = eventWorldSetup.getEventArenaSpawns();
                            if (arenaSpawns.isEmpty()) {
                                eventInfo.append("None");
                            } else {
                                for (int i = 0; i < arenaSpawns.size(); i++) {
                                    Vector3 spawn = arenaSpawns.get(i);
                                    eventInfo.append(spawn.getFloorX()).append(":").append(spawn.getFloorY()).append(":").append(spawn.getFloorZ());
                                    if (i < arenaSpawns.size() - 1) {
                                        eventInfo.append(", ");
                                    }
                                }
                            }
                        } else if (eventWorldSetup.getType().equals(EventType.SUMO)) {
                            eventInfo.append("&fEvent World First Position: &3");
                            if (eventWorldSetup.getFirstPosition() != null) {
                                eventInfo.append(eventWorldSetup.getFirstPosition().getFloorX()).append(", ").append(eventWorldSetup.getFirstPosition().getFloorY()).append(", ").append(eventWorldSetup.getFirstPosition().getFloorZ());
                            } else {
                                eventInfo.append("Not defined");
                            }
                            eventInfo.append("\n");
                            eventInfo.append("&fEvent World Second Position: &3");
                            if (eventWorldSetup.getSecondPosition() != null) {
                                eventInfo.append(eventWorldSetup.getSecondPosition().getFloorX()).append(", ").append(eventWorldSetup.getSecondPosition().getFloorY()).append(", ").append(eventWorldSetup.getSecondPosition().getFloorZ());
                            } else {
                                eventInfo.append("Not defined");
                            }
                        }

                        player.sendMessage(TextFormat.colorize(eventInfo.toString()));
                        event.setCancelled();
                        return;
                    }
                }
            } else if (kitEditorSetup != null) {
                String[] args = message.split(" ");

                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "cancel" -> {
                        kitEditorSetup.destroy();
                        event.setCancelled();
                        return;
                    }
                    case "save" -> {
                        kitEditorSetup.save();
                        event.setCancelled();
                        return;
                    }
                    case "reset" -> {
                        player.getInventory().setContents(kitEditorSetup.getKit().getInventory());
                        player.getInventory().setArmorContents(new Item[]{});
                        event.setCancelled();
                        return;
                    }
                    default -> {
                        kitEditorSetup.sendMessages();
                        event.setCancelled();
                        return;
                    }
                }
            }
        } else if (profile.getProfileData().inStaffMode() && StaffHandler.getInstance().get(profile).isChat()) {
            StaffHandler.getInstance().sendMessage("&6[STAFF] &7" + profile.getName() + ":&f "+ message);
            event.setCancelled();
            return;
        }

        if (ServerEssential.getInstance().isGlobalMute() && !player.hasPermission("globalmute.permission")) {
            player.sendMessage(TextFormat.colorize("&cYou cannot speak during global mute"));
            event.setCancelled();
            return;
        }

        if (!player.hasPermission("chat.cooldown.permission")) {
            if (profile.getCacheData().getChatCooldown() > System.currentTimeMillis()) {
                player.sendMessage(TextFormat.colorize("&cYou can only send one message every 3 seconds"));

                event.setCancelled();
                return;
            } else profile.getCacheData().setChatCooldown(System.currentTimeMillis() + 3000);
        }

        final StringBuilder formatMessage = new StringBuilder();

        if (profile.getCosmeticData().hasColorChat()) {
            message = TextFormat.colorize(profile.getCosmeticData().getColorChat().getColor() + message);
        }

        formatMessage.append("&7").append(profile.getName()).append(":&f ").append(message);
        event.setFormat(TextFormat.colorize(formatMessage.toString()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof FireballEntity || event.getDamager() instanceof TntEntity) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void handleLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearlEntity)) return;

        if (!(event.getShooter() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (profile.getCacheData().getEnderPearl().isInCooldown()) {
            event.setCancelled();
            return;
        }

        profile.getCacheData().getEnderPearl().set();
    }

    @EventHandler
    public void handleChangeSkin(PlayerChangeSkinEvent event) {
        Player player = event.getPlayer();

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (profile.getSettingsData().isEnabled(SettingType.DISGUISE.toString())) {
            player.sendMessage(TextFormat.colorize("&cYou can't change skin if you have the disguise activated"));
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        PlayerInteractEvent.Action action = event.getAction();
        Item item = event.getItem();
        Block block = event.getBlock();

        final boolean act = action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) || action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_AIR);

        if (act && item != null && item.getNamedTag() != null && item.getNamedTag().exist("custom_item")) {
            ItemCustom custom = HotbarItem.getItemCustom(item.getNamedTag().getString("custom_item"));

            if (custom != null) {
                custom.handleUse(player);
                event.setCancelled();
            }

            return;
        }

        if (!profile.getProfileData().isInSetupMode() && item != null && act) {
            ItemDefault itemDefault = DefaultItem.getItemDefault(item);

            if (itemDefault != null) {
                itemDefault.executeUse(profile, item);
                event.setCancelled();
            }

            return;
        }

        DuelWorldSetup.Setup duelWorldSetup = DuelWorldSetup.getInstance().get(profile);
        ArenaWorldSetup.Setup arenaWorldSetup = ArenaWorldSetup.getInstance().get(profile);
        EventWorldSetup.Setup eventWorldSetup = EventWorldSetup.getInstance().get(profile);
        KitEditor.Setup kitEditorSetup = KitEditor.getInstance().get(profile);

        if (kitEditorSetup != null) {
            event.setCancelled();
        } else if (duelWorldSetup != null) {
            if (action.equals(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
                if (item != null && item.getNamedTag() != null && item.getNamedTag().exist("setup_item")) {
                    if (item.equals(Block.get(Block.DIAMOND_BLOCK).toItem(), false, false)) {
                        duelWorldSetup.setFirstPosition(block.getLocation().add(0, 1, 0));
                        player.sendMessage(TextFormat.colorize("&aFirst position has been selected"));
                    } else if (item.equals(Block.get(Block.GOLD_BLOCK).toItem(), false, false)) {
                        duelWorldSetup.setSecondPosition(block.getLocation().add(0, 1, 0));
                        player.sendMessage(TextFormat.colorize("&aSecond position has been selected"));
                    } else if (item.equals(Block.get(Block.EMERALD_BLOCK).toItem(), false, false)) {
                        duelWorldSetup.setFirstPortal(block.getLocation());
                        player.sendMessage(TextFormat.colorize("&aFirst Portal has been selected"));
                    } else if (item.equals(Block.get(Block.REDSTONE_BLOCK).toItem(), false, false)) {
                        duelWorldSetup.setSecondPortal(block.getLocation());
                        player.sendMessage(TextFormat.colorize("&aSecond Portal has been selected"));
                    }
                }
            }
        } else if (arenaWorldSetup!= null) {
            if (action.equals(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
                if (item != null && item.getNamedTag() != null && item.getNamedTag().exist("setup_item")) {
                    if (item.equals(Item.get(Item.STICK), false, false)) {
                        arenaWorldSetup.addArenaSpawn(block.getLocation().add(0, 1, 0));
                        player.sendMessage(TextFormat.colorize("&aSpawn Add"));
                    }
                }
            }
        } else if (eventWorldSetup != null) {
            if (action.equals(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
                if (item != null && item.getNamedTag() != null && item.getNamedTag().exist("setup_item")) {

                    if (item.equals(Item.get(Item.EMERALD), false, false)) {
                        eventWorldSetup.setLobbyPosition(block.getLocation().add(0, 1, 0));
                        player.sendMessage(TextFormat.colorize("&aLobby add"));
                    }

                    if (eventWorldSetup.getType().equals(EventType.SKYWARS)) {
                        if (item.equals(Item.get(Item.STICK), false, false)) {
                            eventWorldSetup.addArenaSpawn(block.getLocation().add(0, 1, 0));
                            player.sendMessage(TextFormat.colorize("&aSpawn Add"));
                        }
                    } else if (eventWorldSetup.getType().equals(EventType.SUMO)) {
                        if (item.equals(Block.get(Block.DIAMOND_BLOCK).toItem(), false, false)) {
                            eventWorldSetup.setFirstPosition(block.getLocation().add(0, 1, 0));
                            player.sendMessage(TextFormat.colorize("&aFirst position has been selected"));
                        } else if (item.equals(Block.get(Block.GOLD_BLOCK).toItem(), false, false)) {
                            eventWorldSetup.setSecondPosition(block.getLocation().add(0, 1, 0));
                            player.sendMessage(TextFormat.colorize("&aSecond position has been selected"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void handleBow(EntityShootBowEvent event) {
        if (!event.isCancelled() && event.getProjectile() instanceof EntityArrow && event.getEntity() instanceof Player player) {
            Profile profile = ProfileManager.getInstance().get(player);

            if (profile == null) return;

            Item itemArrow = Item.get(Item.ARROW, 0, 1);
            Item bow = event.getBow();

            double damage = 2;
            Enchantment bowDamage = bow.getEnchantment(Enchantment.ID_BOW_POWER);
            if (bowDamage != null && bowDamage.getLevel() > 0) {
                damage += (double) bowDamage.getLevel() * 0.5 + 0.5;
            }

            Enchantment flameEnchant = bow.getEnchantment(Enchantment.ID_BOW_FLAME);
            boolean flame = flameEnchant != null && flameEnchant.getLevel() > 0;

            ThreadLocalRandom random = ThreadLocalRandom.current();
            Vector3 dir = Vector3.directionFromRotation(player.pitch, player.yaw)
                    .add(0.0025 * random.nextGaussian(), 0.0025 * random.nextGaussian(), 0.0025 * random.nextGaussian());
            CompoundTag nbt = Entity.getDefaultNBT(player.getEyePosition(), dir.multiply(2.0), (float) dir.yRotFromDirection(), (float) dir.xRotFromDirection())
                    .putShort("Fire", flame ? 45 * 60 : 0)
                    .putDouble("damage", damage)
                    .putByte("auxValue", itemArrow.getDamage())
                    .putCompound("item", new CompoundTag()
                            .putInt("id", itemArrow.getId())
                            .putInt("Damage", itemArrow.getDamage())
                            .putInt("Count", 1));

            double force = event.getForce();
            ArrowEntity projectile = new ArrowEntity(player.chunk, nbt, player, force == 2.8, profile);

            if (force < 0.5) {
                Vector3 startPos = player.getEyePosition().add(dir.multiply(-0.1));
                projectile.setPosition(startPos);
                Vector3 directionToPlayer = player.getEyePosition().subtract(startPos).normalize();
                projectile.setMotion(directionToPlayer.multiply(0.45));
            } else projectile.setMotion(projectile.getMotion().multiply(force));

            Enchantment infinityEnchant = bow.getEnchantment(Enchantment.ID_BOW_INFINITY);
            boolean infinity = infinityEnchant != null && infinityEnchant.getLevel() > 0;

            if (infinity) {
                projectile.setPickupMode(EntityArrow.PICKUP_CREATIVE);
            }

            if (!player.isCreative()) {
                if (!infinity || itemArrow.getDamage() != ItemArrow.NORMAL_ARROW) {
                    player.getInventory().removeItem(itemArrow);
                }

                if (!bow.isUnbreakable()) {
                    Enchantment durability = bow.getEnchantment(Enchantment.ID_DURABILITY);
                    if (!(durability != null && durability.getLevel() > 0 && (100 / (durability.getLevel() + 1)) <= Utils.randomInteger(0, 100))) {
                        bow.setDamage(bow.getDamage() + 2);
                        if (bow.getDamage() >= bow.getMaxDurability()) {
                            bow.count--;
                        }
                        player.getInventory().setItemInHand(bow);
                    }
                }
            }

            projectile.spawnToAll();
            player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BOW);

            if (profile.getProfileData().getDuel() != null && profile.getProfileData().getDuel() instanceof Bridge bridge) {
                bridge.addArrowCooldown(profile);
            } else if (profile.getProfileData().getParty() != null && profile.getProfileData().getParty().getDuel() != null) {
                PartyGame duel = profile.getProfileData().getParty().getDuel();

                if (duel instanceof PartyDuel) {
                    if (duel instanceof alexis.practice.party.games.duel.types.Bridge bridge) {
                        bridge.addArrowCooldown(profile);
                    }
                } else if (duel instanceof PartySplit) {
                    if (duel instanceof alexis.practice.party.games.event.split.types.Bridge bridge) {
                        bridge.addArrowCooldown(profile);
                    }
                }
            }
        }

        event.setCancelled();
    }

    @EventHandler
    public void handleFoodData(PlayerFoodLevelChangeEvent event) {
        event.setCancelled();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void handlePacket(DataPacketSendEvent event) {
        if (event.getPacket() instanceof SetTimePacket packet) {
            Profile profile = ProfileManager.getInstance().get(event.getPlayer());

            if (profile != null) {
                int currentTime = ((GameTime) SettingType.GAME_TIME.getSetting()).getCurrentTime(profile);

                if (currentTime == 0) packet.time = Level.TIME_DAY;
                else if (currentTime == 1) packet.time = Level.TIME_SUNSET;
                else packet.time = Level.TIME_MIDNIGHT;
            }
        } else if (event.getPacket() instanceof LevelEventPacket packet) {
            if (packet.evid == LevelEventPacket.EVENT_START_RAIN) {
                packet.evid = LevelEventPacket.EVENT_STOP_RAIN;
            } else if (packet.evid == LevelEventPacket.EVENT_START_THUNDER) {
                packet.evid = LevelEventPacket.EVENT_STOP_THUNDER;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleReceivePacket(DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof InventoryTransactionPacket packet) {
            if (packet.transactionType == InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY) {
                Player player = event.getPlayer();
                Profile profile = ProfileManager.getInstance().get(player);

                if (profile == null) return;

                final StringBuilder hud = new StringBuilder();

                CPSCounter cpsCounter = (CPSCounter) SettingType.CPS_COUNTER.getSetting();
                if (profile.getSettingsData().isEnabled(SettingType.CPS_COUNTER.toString())) {
                    cpsCounter.add(profile);
                    hud.append("&6CPS:&f ").append(cpsCounter.get(profile));
                }

                if (profile.getSettingsData().isEnabled(SettingType.POTS_COUNTER.toString())) {
                    if (!hud.isEmpty()) hud.append(" &6| ");
                    hud.append("&6Pots:&f ").append(Utils.countItems(player.getInventory().getContents().values(), ItemID.SPLASH_POTION));
                }

                if (profile.getSettingsData().isEnabled(SettingType.SOUP_COUNTER.toString())) {
                    if (!hud.isEmpty()) hud.append(" &6| ");
                    hud.append("&6Soup:&f ").append(Utils.countItems(player.getInventory().getContents().values(), 282));
                }

                if (!hud.isEmpty()) {
                    player.sendPopup(TextFormat.colorize(hud.toString()));
                }
            }
        }
    }

    @EventHandler
    public void handleJoin(PlayerJoinEvent event) {
        ProfileManager.getInstance().create(event.getPlayer());
        event.setJoinMessage("");
    }

    @EventHandler
    public void handleQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile != null) {
            if (freezes.contains(profile.getIdentifier())) {
                StaffHandler.getInstance().sendMessage("&6[FREEZE] &c" + player.getName() + "&7 it came out while it was frozen");
            }
            profile.quit();
        }

        event.setQuitMessage("");
    }

    @EventHandler
    public void handleQuery(QueryRegenerateEvent event) {
        event.setMaxPlayerCount(Practice.getInstance().getServer().getOnlinePlayersCount() + 1);
    }

    @EventHandler
    public void handleChunkUnload(ChunkUnloadEvent event) {
        if (!event.getLevel().equals(event.getLevel().getServer().getDefaultLevel())) return;

        event.getChunk().getEntities().values().forEach(entity -> {
            if (entity.getName().equals("Hologram")) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void handleMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (!player.isOnline() || !player.spawned) return;

        if (from.equals(to)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        int size;

        if (profile.getProfileData().getEvent() != null && profile.getProfileData().getEvent().getEventArena() != null) {
            EventArena eventArena = profile.getProfileData().getEvent().getEventArena();

            if (eventArena instanceof Meetup meetup) {
                if (!eventArena.getWorld().equals(player.getLevel())) return;

                Border border = meetup.getBorder();

                size = border.getSize();
            } else if (eventArena instanceof Tournament tournament) {
                Match match = tournament.inAnyMatch(profile);

                if (match == null) return;

                if (!match.getWorld().equals(player.getLevel())) return;

                if (!eventArena.getEvent().getKit().getName().equals("finaluhc")) return;

                size = 50;
            } else return;

        } else if (profile.getProfileData().getDuel() != null && (profile.getProfileData().getDuel().getKit().getName().equals("finaluhc"))) {
            if (!profile.getProfileData().getDuel().getWorld().equals(player.getLevel())) return;

            size = 50;
        } else if (profile.getProfileData().getParty() != null && profile.getProfileData().getParty().getDuel() != null && (profile.getProfileData().getParty().getDuel().getKit().getName().equals("finaluhc"))){
            if (!profile.getProfileData().getParty().getDuel().getWorld().equals(player.getLevel())) return;

            size = 50;
        } else return;

        if (Math.abs(from.x - to.x) == 0.5 && Math.abs(from.z - to.z) == 0.5) return;

        if ((from.x - 0.5 == to.x && from.z - 0.5 == to.z) || (from.x + 0.5 == to.x && from.z + 0.5 == to.z)) return;
        final int WALL_BORDER_HORIZONTAL_DISTANCE = 7;
        final int minX = to.getFloorX() - WALL_BORDER_HORIZONTAL_DISTANCE;
        final int maxX = to.getFloorX() + WALL_BORDER_HORIZONTAL_DISTANCE;
        final int minZ = to.getFloorZ() - WALL_BORDER_HORIZONTAL_DISTANCE;
        final int maxZ = to.getFloorZ() + WALL_BORDER_HORIZONTAL_DISTANCE;
        boolean inside = true;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                if (insideBorder(new Position(x, 0, z, player.getLevel()), size)) {
                    continue;
                }
                inside = false;
                break;
            }
        }

        if (inside) return;

        int closerX = closestNumber(to.getFloorX(), size, -size);
        int closerZ = closestNumber(to.getFloorZ(), size, -size);

        boolean updateX = Math.abs(to.getFloorX() - closerX) < 10;
        boolean updateZ = Math.abs(to.getFloorZ() - closerZ) < 10;

        if (!updateX && !updateZ) return;
        List<Vector3> toUpdate = new ArrayList<>();

        if (updateX) {
            for (int y = -5; y < 5; ++y) {
                for (int x = -5; x < 5; ++x) {
                    if (between(size, -size, to.getFloorZ() + x)) {
                        continue;
                    }
                    Vector3 target = new Vector3(closerX, to.getFloorY() + y, to.getFloorZ() + x);

                    if (toUpdate.contains(target)) {
                        continue;
                    }
                    Block block = to.getLevel().getBlock(target);

                    if (!(block instanceof BlockAir) && !(block instanceof BlockWater) && !(block instanceof BlockPlanks) && !(block instanceof BlockWaterLily) && !(block instanceof BlockVine) && !(block instanceof BlockSugarcane) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockFlower) && !(block instanceof BlockDoublePlant) && !(block instanceof BlockChorusPlant) & !(block instanceof BlockTallGrass)) {
                        continue;
                    }
                    profile.glassCached.put(block, new Position(target.x, target.y, target.z, player.level));
                    toUpdate.add(target);
                }
            }
        }

        if (updateZ) {
            for (int y = -5; y < 5; ++y) {
                for (int x = -5; x < 5; ++x) {
                    if (between(size, -size, to.getFloorX() + x)) {
                        continue;
                    }
                    Vector3 target = new Vector3(to.getFloorX() + x, to.getFloorY() + y, closerZ);

                    if (toUpdate.contains(target)) {
                        continue;
                    }
                    Block block = to.getLevel().getBlock(target);

                    if (!(block instanceof BlockAir) && !(block instanceof BlockWater) && !(block instanceof BlockPlanks) && !(block instanceof BlockWaterLily) && !(block instanceof BlockVine) && !(block instanceof BlockSugarcane) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockFlower) && !(block instanceof BlockDoublePlant) && !(block instanceof BlockChorusPlant) && !(block instanceof BlockTallGrass)) {
                        continue;
                    }
                    profile.glassCached.put(block, new Position(target.x, target.y, target.z, player.level));
                    toUpdate.add(target);
                }
            }
        }

        for (Vector3 pos : toUpdate) {
            BlockGlassStained b = new BlockGlassStained();
            b.setDamage(DyeColor.BLACK.getDyeData());

            UpdateBlockPacket pk = new UpdateBlockPacket();
            pk.x = (int) pos.x;
            pk.y = (int) pos.y;
            pk.z = (int) pos.z;
            pk.flags = UpdateBlockPacket.FLAG_NETWORK;
            pk.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(player.protocol, b.getFullId());

            player.dataPacket(pk);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void handleRemoveBorder(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (!player.isOnline() || !player.spawned) return;

        if (from.equals(to)) return;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (profile.glassCached.isEmpty()) return;

        ProfileData profileData = profile.getProfileData();
        if ((profileData.getEvent() != null && profileData.getEvent().getKit().getName().equals("finaluhc")) ||
                (profileData.getDuel() != null && (profileData.getDuel().getKit().getName().equals("finaluhc"))) ||
                (profileData.getParty() != null && profileData.getParty().getDuel() != null && (profileData.getParty().getDuel().getKit().getName().equals("finaluhc")))) {
                Position position = player.getPosition();
                List<Map.Entry<Block, Position>> entries = new ArrayList<>(profile.glassCached.entrySet());

                for (Map.Entry<Block, Position> entry : entries) {
                    Position pos = entry.getValue();
                    Block block = entry.getKey();

                    if (position.getLevel() != player.getLevel() ||
                            Math.abs(position.getFloorX() - pos.getFloorX()) <= 5 &&
                                    Math.abs(position.getFloorY() - pos.getFloorY()) <= 6 &&
                                    Math.abs(position.getFloorZ() - pos.getFloorZ()) <= 5) continue;

                    Block currentBlock = pos.getLevelBlock();
                    profile.glassCached.remove(block);

                    if (currentBlock instanceof BlockBedrock) continue;

                    UpdateBlockPacket pk = new UpdateBlockPacket();
                    pk.x = (int) pos.x;
                    pk.y = (int) pos.y;
                    pk.z = (int) pos.z;
                    pk.flags = UpdateBlockPacket.FLAG_NETWORK;
                    pk.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(player.protocol, block.getFullId());

                    player.dataPacket(pk);
            }
        } else profile.glassCached.clear();
    }

    private boolean between(int xone, int xother, int mid) {
        return Math.abs(xone - xother) != Math.abs(mid - xone) + Math.abs(mid - xother);
    }

    private int closestNumber(int from, int... numbers) {
        int distance = Math.abs(numbers[0] - from);
        int idx = 0;

        for (int i = 0; i < numbers.length; i++) {
            int dis = Math.abs(numbers[i] - from);

            if (dis >= distance) {
                continue;
            }
            idx = i;
            distance = dis;
        }
        return numbers[idx];
    }

    private boolean insideBorder(Position position, int size) {
        return position.getFloorX() <= size && position.getFloorX() >= -size && position.getFloorZ() <= size && position.getFloorZ() >= -size;
    }

}
